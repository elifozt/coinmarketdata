package elif.marketdata.common.dto;

public class ContactData {
    private String emailAddress;

    // we need empty constructor
    public ContactData() {
    }

    public ContactData(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    @Override
    public String toString() {
        return "ContactData{" +
                "emailAddress='" + emailAddress + '\'' +
                '}';
    }
}
