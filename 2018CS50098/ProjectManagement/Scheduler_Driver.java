package ProjectManagement;

import PriorityQueue.PriorityQueueDriverCode;

//-------------------------------------------------------------------------------------------//
import Trie.*;
import RedBlack.*;
import PriorityQueue.*;
import java.util.ArrayList;
import java.util.List;
//-------------------------------------------------------------------------------------------//

import java.io.*;
import java.net.URL;

public class Scheduler_Driver extends Thread implements SchedulerInterface {


    public static void main(String[] args) throws IOException {
        Scheduler_Driver scheduler_driver = new Scheduler_Driver();

        File file;
        if (args.length == 0) {
            URL url = PriorityQueueDriverCode.class.getResource("INP");
            file = new File(url.getPath());
        } else {
            file = new File(args[0]);
        }

        scheduler_driver.execute(file);
    }

    public void execute(File file) throws IOException {

        //URL url = Scheduler_Driver.class.getResource("INP");
        //file = new File(url.getPath());


        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(file));
        } catch (FileNotFoundException e) {
            System.err.println("Input file Not found. "+file.getAbsolutePath());
        }
        String st;
        while ((st = br.readLine()) != null) {
            String[] cmd = st.split(" ");
            if (cmd.length == 0) {
                System.err.println("Error parsing: " + st);
                return;
            }

            switch (cmd[0]) {
                case "PROJECT":
                    handle_project(cmd);
                    break;
                case "JOB":
                    handle_job(cmd);
                    break;
                case "USER":
                    handle_user(cmd[1]);
                    break;
                case "QUERY":
                    handle_query(cmd[1]);
                    break;
                case "":
                    handle_empty_line();
                    break;
                case "ADD":
                    handle_add(cmd);
                    break;
                default:
                    System.err.println("Unknown command: " + cmd[0]);
            }
        }


        run_to_completion();

        print_stats();

    }

//----------------------------------------------------------------------------------------------------------------//
    Trie<Project> trie=new Trie<Project>();
    RBTree<Project,Job> rbtree=new RBTree<Project,Job>();
    Trie<User> trie2=new Trie<User>();
    Trie<Job> trie3=new Trie<Job>();
    MaxHeap<Job> maxheap=new MaxHeap<Job>();
    List<Job> printc=new ArrayList<Job>();
    MaxHeap<Project> printr=new MaxHeap<Project>();
    int time=0;
    int job_remaining=0;


    @Override
    public void run() {
    	schedule();
    }


    @Override
    public void run_to_completion() {
    	while(job_remaining!=0) {
    		schedule();
    	}
    }

    @Override
    public void handle_project(String[] cmd) {
    	System.out.println("Creating project");
    	Project project=new Project(cmd[1],Integer.parseInt(cmd[2]),Integer.parseInt(cmd[3]));
    	trie.insert(project.getName(),project);
    	printr.insert(project);
    }

    @Override
    public void handle_job(String[] cmd) {
    	System.out.println("Creating job");
    	if((trie.search(cmd[2])==null)&&(trie2.search(cmd[3])==null)) {
    		System.out.println("No such project exists. "+cmd[2]);
    		System.out.println("No such user exists: "+cmd[3]);
    		return;
    	}
    	if(trie.search(cmd[2])==null) {
    		System.out.println("No such project exists. "+cmd[2]);
    		return;
    	}
    	Project temp_project=trie.search(cmd[2]).obj;
    	if(trie2.search(cmd[3])==null) {
    		System.out.println("No such user exists: "+cmd[3]);
    		return;
    	}
    	User temp_user=trie2.search(cmd[3]).obj;
    	Job job=new Job(cmd[1],temp_project,temp_user,Integer.parseInt(cmd[4]));
    	maxheap.insert(job);
    	job_remaining++;
    	trie3.insert(job.getName(),job);
    }

    @Override
    public void handle_user(String name) {
    	System.out.println("Creating user");
    	User user=new User(name);
    	trie2.insert(user.getName(),user);
    }

    @Override
    public void handle_query(String key) {

    	System.out.println("Querying");
    	if(trie3.search(key)!=null) {
    		Job temp_job=trie3.search(key).obj;
    		System.out.println(temp_job.name+": "+temp_job.jobstatus);
    	}
    	else {
    		System.out.println(key+": NO SUCH JOB");
    	}
    }

    @Override
    public void handle_empty_line() {
    	System.out.println("Running code");
		System.out.println("Remaining jobs: "+job_remaining);
    	while(true) {
	    	Job temp_job=maxheap.extractMax();
	    	if(temp_job==null) {
	    		System.out.println("Execution cycle completed");
	    		return;
	    	}
	    	job_remaining--;
			System.out.println("Executing: "+temp_job.name+" from: "+temp_job.project.name);
	    	if(temp_job.runtime<=temp_job.project.budget) {
	    		printc.add(temp_job);
	    		temp_job.project.budget=temp_job.project.budget-temp_job.runtime;
	    		System.out.println("Project: "+temp_job.project.name+" budget remaining: "+temp_job.project.budget);
	    		temp_job.jobstatus="COMPLETED";
	    		time=time+temp_job.runtime;
	    		temp_job.endtime=time;
	    		System.out.println("Execution cycle completed");
	    		return;
	    	}
	    	else {
	    		rbtree.insert(temp_job.project,temp_job);
	    		System.out.println("Un-sufficient budget.");
	    		temp_job.jobstatus="REQUESTED";
	    	}

    	}
    }

    @Override
    public void handle_add(String[] cmd) {
    	System.out.println("ADDING Budget");
    	if(trie.search(cmd[1])==null) {
    		System.out.println("No such project exists. "+cmd[2]);
    		return;
    	}
    	Project temp_project=trie.search(cmd[1]).obj;
    	temp_project.budget=temp_project.budget+Integer.parseInt(cmd[2]);
    	List<Job> list=rbtree.search(temp_project).getValues();
    	if(list!=null) {
	    	int size=list.size();
	    	for(int i=0;i<size;i++) {
	    		maxheap.insert(list.remove(0));
	    		job_remaining++;
	    	}
    	}
    }

    @Override
    public void print_stats() {
    	System.out.println("--------------STATS---------------");
    	System.out.println("Total jobs done: "+printc.size());
    	for(int i=0;i<printc.size();i++) {
    		Job temp=printc.get(i);
    		System.out.println("Job{user='"+temp.user.name+"', project='"+temp.project.name+"', jobstatus="+temp.jobstatus+", execution_time="+temp.runtime+", end_time="+temp.endtime+", name='"+temp.name+"'}");
    	}
    	System.out.println("------------------------");
    	System.out.println("Unfinished jobs: ");
    	int uf=0;
    	Project temp=printr.extractMax();
    	while(temp!=null) {
    		if(rbtree.search(temp).getValues()!=null) {
	    		List<Job> list=rbtree.search(temp).getValues();
	    		for(int i=0;i<list.size();i++) {
	    			Job temp2=list.get(i);
	    			System.out.println("Job{user='"+temp2.user.name+"', project='"+temp2.project.name+"', jobstatus="+temp2.jobstatus+", execution_time="+temp2.runtime+", end_time=null, name='"+temp2.name+"'}");
	    			uf++;
	    		}
    		}
    		temp=printr.extractMax();
    	}
    	System.out.println("Total unfinished jobs: "+uf);
    	System.out.println("--------------STATS DONE---------------");
    }

    @Override
    public void schedule() {
    	System.out.println("Running code");
		System.out.println("Remaining jobs: "+job_remaining);
    	while(true) {
	    	Job temp_job=maxheap.extractMax();
	    	if(temp_job==null) {
	    		System.out.println("System execution completed");
	    		return;
	    	}
	    	job_remaining--;
			System.out.println("Executing: "+temp_job.name+" from: "+temp_job.project.name);
	    	if(temp_job.runtime<=temp_job.project.budget) {
	    		printc.add(temp_job);
	    		temp_job.project.budget=temp_job.project.budget-temp_job.runtime;
	    		System.out.println("Project: "+temp_job.project.name+" budget remaining: "+temp_job.project.budget);
	    		temp_job.jobstatus="COMPLETED";
	    		time=time+temp_job.runtime;
	    		temp_job.endtime=time;
	    		System.out.println("System execution completed");
	    		return;
	    	}
	    	else {
	    		rbtree.insert(temp_job.project,temp_job);
	    		System.out.println("Un-sufficient budget.");
	    		temp_job.jobstatus="REQUESTED";
	    	}
    	}
    }
}
//-------------------------------------------------------------------------------------------------------------------------------------//
