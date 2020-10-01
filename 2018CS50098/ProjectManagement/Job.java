package ProjectManagement;

public class Job implements Comparable<Job> {
	public String name;
	public Project project;
	public User user;
	public Integer runtime;
	public String jobstatus;
	public Integer endtime;
	public Job(String name,Project project,User user,int runtime) {
		this.name=name;
		this.project=project;
		this.user=user;
		this.runtime=runtime;
		this.jobstatus="NOT FINISHED";
		this.endtime=99999999;
	}
	public String getName() {
		return name;
	}
    @Override
    public int compareTo(Job job) {
        return (this.project.priority-job.project.priority);
    }
}