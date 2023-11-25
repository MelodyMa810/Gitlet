package gitlet;

import java.io.Serializable;
import java.util.HashMap;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Commit class.
 * @author Melody Ma. */
public class Commit implements Serializable {

    /** commit ID. */
    private String _ID;
    /** commit message. */
    private String _message;
    /** time of the commit. */
    private String timestamp;
    /** blobs of the commit. */
    private HashMap<String, String> _blobs;
    /** parent of the current commit. */
    private String _parent;
    /** first parent. */
    private String _firstParent = null;
    /** second parent. */
    private String _secondParent = null;

    /** Commit elements.
     * @param message commit message.
     * @param blobs commit blobs.
     * @param parent parent of the commit.
     */
    public Commit(String message, String parent,
                  HashMap<String, String> blobs) {
        this._message = message;
        this._parent = parent;
        this._ID = Utils.sha1(Utils.serialize(this));
        LocalDateTime currentDateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern
                ("E MMM dd HH:mm:ss yyyy -0800");
        String formattedDateTime = currentDateTime.format(formatter);
        this.timestamp = formattedDateTime;
        this._blobs = blobs;
    }

    /** get the commit ID.
     * @return commit ID.
     */
    public String getID() {
        return this._ID;
    }

    /** get the commit message.
     * @return commit message.
     */
    public String getMessage() {
        return this._message;
    }

    /** get the time stamp of the commit.
     * @return time stamp of the commit.
     */
    public String getTimestamp() {
        return this.timestamp;
    }

    /** get the parent of the current commit.
     * @return parent of the commit.
     */
    public String getParent() {
        return this._parent;
    }

    /** get the parent of the current commit.
     * @return second parent of the commit.
     */
    public String getSecondParent() {
        return this._secondParent;
    }

    /** get the HashMap that stores the files
     * and contents of the current commit.
     * @return blobs of the commit.
     */
    public HashMap<String, String> getBlobs() {
        return this._blobs;
    }

    /** set the first parent.
     * @param parent fist parent.
     */
    public void setFirstParent(String parent) {
        this._firstParent = parent;
    }

    /** set the second parent.
     * @param parent second parent.
     */
    public void setSecondParent(String parent) {
        this._secondParent = parent;
    }
}
