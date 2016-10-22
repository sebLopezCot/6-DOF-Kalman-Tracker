package slcot.mit.edu.a6_dofkalmantracker.socketio;

import android.util.Log;

import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

/**
 * Created by Sebastian on 10/21/2016.
 */
public class TrackerServer {

    static TrackerServer mServer;
    static Socket mSocket;

    private TrackerServer(){
        if(mSocket == null){
            try{
                mSocket = IO.socket("http://kalman-tracker-server.herokuapp.com");
            }catch (Exception e){
                e.printStackTrace();
                Log.d("error connecting","to server");
            }
        }
    }

    public static TrackerServer getInstance(){
        if (mServer == null){
            mServer = new TrackerServer();
        }

        return mServer;
    }

    public void send(Object... message){
        if(mSocket != null){
            if(!mSocket.connected()){
                mSocket.connect();
            }

            mSocket.emit("measurement", message);
        }
    }

    public void disconnect(){
        if(mSocket != null && mSocket.connected()){
            mSocket.disconnect();
        }
    }
}
