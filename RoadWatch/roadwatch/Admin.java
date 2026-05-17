package roadwatch;

//admin
public class Admin {

    private int    id;
    private String username;
    private String password;
    private String fullName;

    public Admin() {}

    public Admin(int id, String username, String fullName) {
        this.id       = id;
        this.username = username;
        this.fullName = fullName;
    }

    // Getters & Setters
    public int    getId()              { return id; }
    public void   setId(int id)        { this.id = id; }

    public String getUsername()        { return username; }
    public void   setUsername(String v){ this.username = v; }

    public String getPassword()        { return password; }
    public void   setPassword(String v){ this.password = v; }

    public String getFullName()        { return fullName; }
    public void   setFullName(String v){ this.fullName = v; }

    @Override
    public String toString() { return fullName + " (" + username + ")"; }
}
