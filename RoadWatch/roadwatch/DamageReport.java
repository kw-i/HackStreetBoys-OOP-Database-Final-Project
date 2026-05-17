package roadwatch;

import java.sql.Timestamp;

 //DamageReport – Model/entity class for one infrastructure damage report.
 //v2: added citizenId (0 = guest) and photo (byte[] BLOB, null = none).
 
public class DamageReport {

    private int       id;
    private String    reportCode;
    private int       citizenId;     // 0 = guest submission
    private String    reporterName;
    private String    contactNumber;
    private String    damageType;
    private String    location;
    private String    severity;
    private String    description;
    private byte[]    photo;         // null = no photo attached
    private String    status;
    private String    adminRemarks;
    private Timestamp submittedAt;
    private Timestamp updatedAt;

    public DamageReport() {}

    public DamageReport(String reporterName, String contactNumber,
                        String damageType, String location,
                        String severity, String description) {
        this.reporterName  = reporterName;
        this.contactNumber = contactNumber;
        this.damageType    = damageType;
        this.location      = location;
        this.severity      = severity;
        this.description   = description;
        this.status        = "Pending";
    }

    //  Getters & Setters 
    public int    getId()                         { return id; }
    public void   setId(int id)                   { this.id = id; }

    public String getReportCode()                 { return reportCode; }
    public void   setReportCode(String v)         { this.reportCode = v; }

    public int    getCitizenId()                  { return citizenId; }
    public void   setCitizenId(int v)             { this.citizenId = v; }

    public String getReporterName()               { return reporterName; }
    public void   setReporterName(String v)       { this.reporterName = v; }

    public String getContactNumber()              { return contactNumber; }
    public void   setContactNumber(String v)      { this.contactNumber = v; }

    public String getDamageType()                 { return damageType; }
    public void   setDamageType(String v)         { this.damageType = v; }

    public String getLocation()                   { return location; }
    public void   setLocation(String v)           { this.location = v; }

    public String getSeverity()                   { return severity; }
    public void   setSeverity(String v)           { this.severity = v; }

    public String getDescription()                { return description; }
    public void   setDescription(String v)        { this.description = v; }

    public byte[] getPhoto()                      { return photo; }
    public void   setPhoto(byte[] v)              { this.photo = v; }

    public String getStatus()                     { return status; }
    public void   setStatus(String v)             { this.status = v; }

    public String getAdminRemarks()               { return adminRemarks; }
    public void   setAdminRemarks(String v)       { this.adminRemarks = v; }

    public Timestamp getSubmittedAt()             { return submittedAt; }
    public void      setSubmittedAt(Timestamp v)  { this.submittedAt = v; }

    public Timestamp getUpdatedAt()               { return updatedAt; }
    public void      setUpdatedAt(Timestamp v)    { this.updatedAt = v; }

    @Override
    public String toString() {
        return "[" + reportCode + "] " + damageType + " @ " + location + " — " + status;
    }
}
