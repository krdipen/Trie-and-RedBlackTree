package ProjectManagement;

public class Project implements Comparable<Project>{
	public String name;
	public Integer priority;
	public Integer budget;
	public Project(String name,int priority,int budget) {
		this.name=name;
		this.priority=priority;
		this.budget=budget;
	}
	public String getName() {
		return name;
	}
	@Override
	public int compareTo(Project project) {
		return this.priority-project.priority;
	}
}