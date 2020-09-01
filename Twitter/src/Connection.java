import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.nio.Buffer;

public class Connection {
    private DataInputStream dataInputStream;
    private DataOutputStream outputStream;

    public Connection(DataInputStream dataInputStream, DataOutputStream outputStream) {
        this.dataInputStream = dataInputStream;
        this.outputStream = outputStream;
    }

    public DataInputStream getInputStream() {
        return this.dataInputStream;
    }

    public DataOutputStream getOutputStream() {
        return this.outputStream;
    }

}
