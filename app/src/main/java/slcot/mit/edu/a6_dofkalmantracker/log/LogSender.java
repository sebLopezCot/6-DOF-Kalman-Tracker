package slcot.mit.edu.a6_dofkalmantracker.log;

import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.util.Scanner;

/**
 * Created by Sebastian on 10/10/2016.
 */
public class LogSender {
    private static LogSender logSender = null;

    private Socket socket;

    private LogSender(){
        try {
            socket = IO.socket("myspecialurl.herokuapp.com");

        } catch (URISyntaxException ex) {

        }
    }

    public static LogSender getInstance(){
        if(LogSender.logSender == null){
            LogSender.logSender = new LogSender();
        }

        return LogSender.logSender;
    }

    public void send(MatrixCSVLogger log){
        this.socket.connect();

        try {
            String content = new Scanner(log.getFile()).useDelimiter("\\Z").next();

            socket.emit("log-sender-message", content, log.getFilename());

        } catch (FileNotFoundException ex) {

        }

        this.socket.disconnect();
    }
}
