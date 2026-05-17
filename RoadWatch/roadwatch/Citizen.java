package roadwatch;

import java.sql.Timestamp;

//Citizen – Model for registered public user accounts.
 
public class Citizen {

    private int       id;
    private String    fullName;
    private String    email;
    private String    contactNumber;
    private String    password;
    private Timestamp createdAt;

    public Citizen() {}

    public Citizen(int id, String fullName, String email, String contactNumber) {
        this.id            = id;
        this.fullName      = fullName;
        this.email         = email;
        this.contactNumber = contactNumber;
    }

    public int    getId()                       { return id; }
    public void   setId(int v)                  { this.id = v; }
    public String getFullName()                 { return fullName; }
    public void   setFullName(String v)         { this.fullName = v; }
    public String getEmail()                    { return email; }
    public void   setEmail(String v)            { this.email = v; }
    public String getContactNumber()            { return contactNumber; }
    public void   setContactNumber(String v)    { this.contactNumber = v; }
    public String getPassword()                 { return password; }
    public void   setPassword(String v)         { this.password = v; }
    public Timestamp getCreatedAt()             { return createdAt; }
    public void      setCreatedAt(Timestamp v)  { this.createdAt = v; }

    @Override public String toString() { return fullName + " <" + email + ">"; }
}
