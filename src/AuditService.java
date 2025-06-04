import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;

public class AuditService {
    private static AuditService instance;
    private PrintWriter writer;

    private AuditService() {
        try {
            this.writer = new PrintWriter(new FileWriter("audit_log.csv", true));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static synchronized AuditService getInstance() {
        if (instance == null) {
            instance = new AuditService();
        }
        return instance;
    }

    public void logAction(String action) {
        writer.println(action + "," + LocalDateTime.now());
        writer.flush();
    }

    public void close() {
        writer.close();
    }
}