import java.util.Objects;

public class Account {
  private String username;
  private String password;
  private int portNumber;

  public Account(String username, String password) {
    this.username = username;
    this.password = password;
  }

  // when a user logs in, set its portnumber
  public void setPortNumber(int portNumber) {
    this.portNumber = portNumber;
  }

  public String getUsername() {
    return username;
  }

  public String getPassword() {
    return password;
  }

  public int getPortNumber() {
    return portNumber;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Account that = (Account) o;
    return username.equals(that.username) &&
        password.equals(that.password);
  }

  @Override
  public int hashCode() {
    return Objects.hash(username, password);
  }
}
