public class Request {

  private String type;
  private String[] header;

  public Request(String type, String[] header) {
    this.type = type;
    this.header = header;
  }

  public static Request parseRequest(String message) {
    // some example messages: register:bob:password
    Utils utils = new Utils();
    String[] result = message.split(utils.SEPARATOR);
    // result = {"register", "bob", "1234"};
    String type = result[0];
    String[] header = new String[result.length - 1];
    for (int i = 0; i < header.length; i ++) {
      header[i] = result[i + 1];
    }
    return new Request(type, header);
  }

  public String getType() {
    return type;
  }

  public String[] getHeader() {
    return header;
  }
}
