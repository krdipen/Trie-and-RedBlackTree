package ProjectManagement;

public class User implements Comparable<User> {
	public String name;
	public User(String name) {
		this.name=name;
	}
	public String getName() {
		return name;
	}
    @Override
    public int compareTo(User user) {
        return this.name.compareTo(user.name);
    }
}