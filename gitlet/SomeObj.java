package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

/** All the functions.
 * @author Melody Ma
 */
public class SomeObj {

    /** Current working directory. */
    static final File CWD = new File(System.getProperty("user.dir"));
    /** .gitlet folder. */
    static final File GITLIT = Utils.join(CWD, ".gitlet");
    /** staging area. */
    static final File STAGING = Utils.join(GITLIT, "Staging Area");
    /** staged for removal area. */
    static final File REMOVAL = Utils.join(GITLIT, "Staging for Removal");
    /** removed files. */
    static final File REMOVED = Utils.join(GITLIT, "Removed Files");
    /** commits folder. */
    static final File COMMITS = Utils.join(GITLIT, "Commits");
    /** blobs folder. */
    static final File BLOBS = Utils.join(GITLIT, "Blobs");
    /** branches folder. */
    static final File BRANCHES = Utils.join(GITLIT, "Branches");
    /** head file. */
    static final File HEAD = Utils.join(GITLIT, "Head");

    /** Creates a new Gitlet version-control system
     * in the current directory. */
    public static void init() throws IOException {
        if (GITLIT.exists()) {
            System.out.println("A Gitlet version-control system already"
                    + "exists in the current directory.");
        } else {
            GITLIT.mkdir();
            STAGING.mkdir();
            REMOVAL.mkdir();
            REMOVED.mkdir();
            COMMITS.mkdir();
            BLOBS.mkdir();
            BRANCHES.mkdir();
            HEAD.createNewFile();
        }

        Commit initial = new Commit("initial commit", null, null);
        String initialID = initial.getID();
        File initialCommit = Utils.join(COMMITS, initialID);
        initialCommit.createNewFile();
        Utils.writeObject(initialCommit, initial);
        File master = Utils.join(BRANCHES, "master");
        master.createNewFile();
        Utils.writeContents(master, initialID);
        String masterString = "master";
        Utils.writeContents(HEAD, masterString);
    }

    /** Helper function to get the current commit.
     * @return the current commit.
     */
    public static Commit getCurrentCommit() {
        String currBranch = Utils.readContentsAsString(HEAD);
        String currCommitID = Utils.readContentsAsString
                (Utils.join(BRANCHES, currBranch));
        return Utils.readObject
                (Utils.join(COMMITS, currCommitID), Commit.class);
    }

    /** to stage a file.
     * @param filename name of the file to be staged.
     */
    public static void add(String filename) throws IOException {
        File curr = Utils.join(CWD, filename);
        if (!curr.exists()) {
            System.out.println("File does not exist.");
        } else {
            String content = Utils.readContentsAsString(curr);
            String contentID = Utils.sha1(Utils.readContents(curr));
            Commit currCommit = getCurrentCommit();
            HashMap<String, String> currBlobs = currCommit.getBlobs();
            if (currBlobs == null || !currBlobs.containsKey(filename)) {
                File addedFile = Utils.join(STAGING, filename);
                addedFile.createNewFile();
                Utils.writeContents(addedFile, content);
            } else {
                File addedFile = Utils.join(STAGING, filename);
                addedFile.createNewFile();
                Utils.writeContents(addedFile, content);
                String ogContent = currBlobs.get(filename);
                if (contentID.equals(ogContent)) {
                    addedFile.delete();
                }
                File ifInRemoval = Utils.join(REMOVAL, filename);
                if (ifInRemoval.exists()) {
                    String removedContent = Utils.readContentsAsString
                            (ifInRemoval);
                    if (content.equals(removedContent)) {
                        addedFile.delete();
                        ifInRemoval.delete();
                        File reCreated = Utils.join(CWD, filename);
                        reCreated.createNewFile();
                        Utils.writeContents(reCreated, content);
                    }
                }
            }
        }
    }

    /** to commit.
     * @param message commit message.
     */
    public static void commit(String message) throws IOException {
        File direc = new File(STAGING.getAbsolutePath());
        int fileCount = direc.list().length;
        File[] direcListing = direc.listFiles();
        File direcRemoval = new File(REMOVAL.getAbsolutePath());
        int fileCountRemoval = direcRemoval.list().length;
        File[] direcListingRemoval = direcRemoval.listFiles();
        if (fileCount == 0 && fileCountRemoval == 0) {
            System.out.println("No changes added to the commit.");
        } else if (message.equals("")) {
            System.out.println("Please enter a commit message.");
        } else {
            Commit curr = getCurrentCommit();
            HashMap<String, String> currBlobs = curr.getBlobs();
            HashMap<String, String> newBlobs;
            if (currBlobs == null) {
                newBlobs = new HashMap<>();
            } else {
                newBlobs = new HashMap<>(currBlobs.size());
                for (String key : currBlobs.keySet()) {
                    if (!Utils.join(REMOVAL, key).exists()) {
                        newBlobs.put(key, currBlobs.get(key));
                    }
                }
            }
            for (File toBeRemoved : direcListingRemoval) {
                toBeRemoved.delete();
            }
            for (File child : direcListing) {
                String newID = Utils.sha1(Utils.readContents(child));
                newBlobs.put(child.getName(), newID);
                String content = Utils.readContentsAsString(child);
                File addedBlob = Utils.join(BLOBS, newID);
                addedBlob.createNewFile();
                Utils.writeContents(addedBlob, content);
            }
            for (File child: direcListing) {
                if (!child.isDirectory()) {
                    child.delete();
                }
            }
            Commit newCommit = new Commit(message, curr.getID(), newBlobs);
            String newCommitID = newCommit.getID();
            File addedCommit = Utils.join(COMMITS, newCommitID);
            addedCommit.createNewFile();
            Utils.writeObject(addedCommit, newCommit);
            String currBranch = Utils.readContentsAsString(HEAD);
            Utils.writeContents(Utils.join(BRANCHES, currBranch), newCommitID);
        }
    }

    /** to remove a file.
     * @param filename name of the file to be removed.
     */
    public static void rm(String filename) throws IOException {
        File fileStaged = Utils.join(STAGING, filename);
        HashMap<String, String> currBlobs = getCurrentCommit().getBlobs();
        if (currBlobs == null && fileStaged.exists()) {
            fileStaged.delete();
            return;
        }
        if (currBlobs == null
                || (!fileStaged.exists() && !currBlobs.containsKey(filename))) {
            System.out.println("No reason to remove the file.");
        } else {
            if (fileStaged.exists()) {
                fileStaged.delete();
            } else {
                if (currBlobs.containsKey(filename)) {
                    String contentAsID = currBlobs.get(filename);
                    byte[] content = Utils.readContents
                            ((Utils.join(BLOBS, contentAsID)));
                    File fileForRemoval = Utils.join(REMOVAL, filename);
                    fileForRemoval.createNewFile();
                    Utils.writeContents(fileForRemoval, content);
                    File fileCWD = Utils.join(CWD, filename);
                    fileCWD.delete();
                }
            }
        }
    }

    /** checkout.
     * @param args input message.
     */
    public static void checkout(String... args) {
        if (args.length == 3) {
            String filename = args[2];
            if (!args[1].equals("--")) {
                System.out.println("Incorrect operands.");
                return;
            }
            Commit headCommit = getCurrentCommit();
            HashMap<String, String> currBlobs = headCommit.getBlobs();
            if (!currBlobs.containsKey(filename)) {
                System.out.println("File does not exist in that commit.");
            } else {
                File updatedFile = Utils.join(CWD, filename);
                String blobID = currBlobs.get(filename);
                String content = Utils.readContentsAsString
                        (Utils.join(BLOBS, blobID));
                Utils.writeContents(updatedFile, content);
            }
        } else if (args.length == 4) {
            String commitID = args[1];
            String filename = args[3];
            if (!args[2].equals("--")) {
                System.out.println("Incorrect operands.");
                return;
            }
            File currFile = Utils.join(COMMITS, commitID);
            if (commitID.length() > 10 && !currFile.exists()) {
                System.out.println("No commit with that id exists.");
                return;
            }
            if (commitID.length() == 8) {
                String fullCommitID = null;
                List<String> allCommits = Utils.plainFilenamesIn(COMMITS);
                for (String fullID : allCommits) {
                    if (fullID.substring(0, 8).equals(commitID)) {
                        fullCommitID = fullID;
                    }
                }
                if (fullCommitID == null) {
                    System.out.println("No commit with that id exists.");
                    return;
                }
                commitID = fullCommitID;
            }
            Commit curr = Utils.readObject
                    (Utils.join(COMMITS, commitID), Commit.class);
            HashMap<String, String> currBlobs = curr.getBlobs();
            if (!currBlobs.containsKey(filename)) {
                System.out.println("File does not exist in that commit.");
            } else {
                File updatedFile = Utils.join(CWD, filename);
                String blobID = currBlobs.get(filename);
                String content = Utils.readContentsAsString
                        (Utils.join(BLOBS, blobID));
                Utils.writeContents(updatedFile, content);
            }
        } else {
            checkoutThirdCase(args);
        }
    }

    /** third case of checkout.
     * @param args input message.
     */
    public static void checkoutThirdCase(String... args) {
        String currBranch = args[1];
        File branchCheck = Utils.join(BRANCHES, currBranch);
        if (!branchCheck.exists()) {
            System.out.println("No such branch exists.");
            return;
        }
        String currHead = Utils.readContentsAsString(HEAD);
        String currCommitID = Utils.readContentsAsString
                (Utils.join(BRANCHES, currHead));
        Commit currCommit = Utils.readObject
                (Utils.join(COMMITS, currCommitID), Commit.class);
        HashMap<String, String> currBlobs = currCommit.getBlobs();
        List<String> currWorkingFiles = Utils.plainFilenamesIn(CWD);
        List<String> currStagedFiles = Utils.plainFilenamesIn(STAGING);
        if (currHead.equals(currBranch)) {
            System.out.println("No need to checkout the current branch.");
            return;
        }
        for (String file : currWorkingFiles) {
            if ((currWorkingFiles.size() > 0 && currBlobs == null)
                    || !currBlobs.containsKey(file)) {
                System.out.println("There is an untracked file in the way;"
                        + " " + "delete it, or add and commit it first.");
                return;
            }
        }
        String givenCommitID = Utils.readContentsAsString(branchCheck);
        Commit commitGiven = Utils.readObject
                (Utils.join(COMMITS, givenCommitID), Commit.class);
        HashMap<String, String> givenBlobs = commitGiven.getBlobs();
        if (givenBlobs == null) {
            for (String file : currWorkingFiles) {
                File currFile = Utils.join(CWD, file);
                currFile.delete();
            }
        } else {
            for (String file : currWorkingFiles) {
                if (!givenBlobs.containsKey(file)) {
                    File currFile = Utils.join(CWD, file);
                    currFile.delete();
                }
            }
            for (String file : givenBlobs.keySet()) {
                String blobID = givenBlobs.get(file);
                String content = Utils.readContentsAsString
                        (Utils.join(BLOBS, blobID));
                File currFile = Utils.join(CWD, file);
                Utils.writeContents(currFile, content);
            }
        }
        Utils.writeContents(HEAD, currBranch);
        for (String file : currStagedFiles) {
            File curr = Utils.join(STAGING, file);
            curr.delete();
        }
    }

    /** log. */
    public static void log() {
        Commit headCommit = getCurrentCommit();
        while (headCommit != null) {
            String commitID = headCommit.getID();
            String timeStamp = headCommit.getTimestamp();
            String message = headCommit.getMessage();
            System.out.println("===");
            System.out.println("commit" + " " + commitID);
            System.out.println("Date:" + " " + timeStamp);
            System.out.println(message);
            System.out.println();
            if (headCommit.getParent() != null) {
                headCommit = Utils.readObject
                        (Utils.join(COMMITS, headCommit.getParent()),
                                Commit.class);
            } else {
                break;
            }
        }
    }

    /** global-log. */
    public static void globalLog() {
        List<String> allCommits = Utils.plainFilenamesIn(COMMITS);
        for (String commit : allCommits) {
            File curr = Utils.join(COMMITS, commit);
            Commit currCommit = Utils.readObject(curr, Commit.class);
            System.out.println("===");
            System.out.println("commit" + " " + currCommit.getID());
            System.out.println("Date:" + " " + currCommit.getTimestamp());
            System.out.println(currCommit.getMessage());
            System.out.println();
        }
    }

    /** find.
     * @param message commit ID.
     */
    public static void find(String message) {
        List<String> allCommits = Utils.plainFilenamesIn(COMMITS);
        HashMap<String, String> dictionary = new
                HashMap<String, String>(allCommits.size());
        for (String commit : allCommits) {
            File curr = Utils.join(COMMITS, commit);
            Commit currCommit = Utils.readObject(curr, Commit.class);
            String currID = currCommit.getID();
            String currMessage = currCommit.getMessage();
            dictionary.put(currID, currMessage);
            if (message.equals(currMessage)) {
                System.out.println(currID);
            }
        }
        if (!dictionary.containsValue(message)) {
            System.out.println("Found no commit with that message.");
        }
    }

    /** create a new branch.
     * @param branch name of the new branch.
     */
    public static void branch(String branch) throws IOException {
        List<String> allBranches = Utils.plainFilenamesIn(BRANCHES);
        for (String curr : allBranches) {
            if (curr.equals(branch)) {
                System.out.println("A branch with that name already exists.");
                return;
            }
        }
        File newBranch = Utils.join(BRANCHES, branch);
        newBranch.createNewFile();
        Commit currCommit = getCurrentCommit();
        String currID = currCommit.getID();
        Utils.writeContents(newBranch, currID);
    }

    /** remove a branch.
     * @param branch branch to be removed.
     */
    public static void rmBranch(String branch) {
        List<String> allBranches = Utils.plainFilenamesIn(BRANCHES);
        if (!allBranches.contains(branch)) {
            System.out.println("A branch with that name does not exist.");
            return;
        }
        String currBranch = Utils.readContentsAsString(HEAD);
        if (currBranch.equals(branch)) {
            System.out.println("Cannot remove the current branch.");
            return;
        }
        File branchToDelete = Utils.join(BRANCHES, branch);
        branchToDelete.delete();
    }

    /** reset a commit.
     * @param commitID ID of the commit to be reset.
     */
    public static void reset(String commitID) throws IOException {
        List<String> allCommits = Utils.plainFilenamesIn(COMMITS);
        if (!allCommits.contains(commitID)) {
            System.out.println("No commit with that id exists.");
            return;
        }
        List<String> currWorkingFiles = Utils.plainFilenamesIn(CWD);
        Commit givenCommit = Utils.readObject
                ((Utils.join(COMMITS, commitID)), Commit.class);
        HashMap<String, String> currBlobs = givenCommit.getBlobs();
        for (String file : currWorkingFiles) {
            Commit curr = getCurrentCommit();
            HashMap<String, String> blobs = curr.getBlobs();
            if (!Utils.join(STAGING, file).exists()
                    && (blobs == null || !blobs.containsKey(file))) {
                System.out.println("There is an untracked file in the way;"
                        + "delete it, or add and commit it first.");
                return;
            }
            if (!currBlobs.containsKey(file)) {
                Utils.join(CWD, file).delete();
            }
        }
        for (String file : currBlobs.keySet()) {
            String[] input = new String[4];
            input[0] = "checkout";
            input[1] = commitID;
            input[2] = "--";
            input[3] = file;
            Main.main(input);
        }
        List<String> currStagedFiles = Utils.plainFilenamesIn(STAGING);
        for (String file : currStagedFiles) {
            File curr = Utils.join(STAGING, file);
            curr.delete();
        }
        String currBranch = Utils.readContentsAsString(HEAD);
        Utils.writeContents(Utils.join(BRANCHES, currBranch), commitID);
    }

    /** get the current status. */
    public static void status() {
        if (!GITLIT.exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            return;
        }
        System.out.println("=== Branches ===");
        System.out.println("*master");
        List<String> allBranches = Utils.plainFilenamesIn(BRANCHES);
        for (String branch : allBranches) {
            if (!branch.equals("master")) {
                System.out.println(branch);
            }
        }
        System.out.println();
        System.out.println("=== Staged Files ===");
        List<String> allStagedFiles = Utils.plainFilenamesIn(STAGING);
        for (String file : allStagedFiles) {
            System.out.println(file);
        }
        System.out.println();
        System.out.println("=== Removed Files ===");
        List<String> allRemovedFiles = Utils.plainFilenamesIn(REMOVAL);
        for (String file : allRemovedFiles) {
            System.out.println(file);
        }
        System.out.println();
        System.out.println("=== Modifications Not Staged For Commit ===");
        modification();
        System.out.println("=== Untracked Files ===");
        List<String> allWorkingFiles = Utils.plainFilenamesIn(CWD);
        HashMap<String, String> currBlobs = getCurrentCommit().getBlobs();
        for (String file : allWorkingFiles) {
            if (currBlobs == null) {
                if (!Utils.join(STAGING, file).exists()) {
                    System.out.println(file);
                }
            } else {
                if ((!currBlobs.containsKey(file)
                        && !Utils.join(STAGING, file).exists())
                        || Utils.join(REMOVAL, file).exists()) {
                    System.out.println(file);
                }
            }
        }
        System.out.println();
    }

    /** Helper function that handles modification.
     */
    public static void modification() {
        List<String> allStagedFiles = Utils.plainFilenamesIn(STAGING);
        HashMap<String, String> currBlobs = getCurrentCommit().getBlobs();
        if (currBlobs != null) {
            for (String file : currBlobs.keySet()) {
                if (!Utils.join(CWD, file).exists()
                        && !Utils.join(REMOVAL, file).exists()) {
                    System.out.println(file + " " + "(deleted)");
                    continue;
                }
                if (!Utils.join(CWD, file).exists()
                        && Utils.join(REMOVAL, file).exists()) {
                    continue;
                }
                String contentInCWD = Utils.sha1
                        (Utils.readContents(Utils.join(CWD, file)));
                String contentTracked = currBlobs.get(file);
                if (!contentTracked.equals(contentInCWD)
                        && !Utils.join(STAGING, file).exists()) {
                    System.out.println(file + " " + "(modified)");
                }
            }
        }
        for (String file : allStagedFiles) {
            if (!Utils.join(CWD, file).exists()) {
                System.out.println(file + " " + "(deleted)");
                continue;
            }
            String contentInCWD = Utils.sha1
                    (Utils.readContents(Utils.join(CWD, file)));
            String contentStaged = Utils.sha1
                    (Utils.readContents(Utils.join(STAGING, file)));
            if (!contentInCWD.equals(contentStaged)) {
                System.out.println(file + " " + "(modified)");
            }
        }
        System.out.println();
    }

    /** Helper function to find the split point.
     * @return the current commit.
     * @param branch the given branch.
     */
    public static Commit getSplitPoint(String branch) {
        Commit currCommit = getCurrentCommit();
        String givenCommitID = Utils.readContentsAsString
                (Utils.join(BRANCHES, branch));
        Commit givenCommit = Utils.readObject
                (Utils.join(COMMITS, givenCommitID), Commit.class);
        Commit splitPoint = null;
        while (givenCommit.getParent() != null) {
            String givenCommmitParent = givenCommit.getParent();
            while (currCommit.getParent() != null) {
                String currCommitParent = currCommit.getParent();
                String currCommitSecondParent = currCommit.getSecondParent();
                if (givenCommmitParent.equals(currCommitParent)) {
                    String splitPointID = currCommitParent;
                    splitPoint = Utils.readObject
                            (Utils.join(COMMITS, splitPointID),
                                    Commit.class);
                    return splitPoint;
                } else if (givenCommmitParent.equals(currCommitSecondParent)) {
                    String splitPointID = currCommitSecondParent;
                    splitPoint = Utils.readObject
                            (Utils.join(COMMITS, splitPointID),
                                    Commit.class);
                    return splitPoint;
                } else {
                    currCommit = Utils.readObject
                            (Utils.join(COMMITS, currCommit.getParent()),
                                    Commit.class);
                }
            }
            givenCommit = Utils.readObject
                    (Utils.join(COMMITS, givenCommit.getParent()),
                            Commit.class);
        }
        return splitPoint;
    }

    /** Helper function to do failure check for merge.
     * @param branch given branch
     * @return whether there are failure cases
     */
    public static boolean mergeFailureCheck(String branch)
            throws IOException {
        boolean failureCheck = false;
        List<String> stagingArea = Utils.plainFilenamesIn(STAGING);
        List<String> stagingRemoval = Utils.plainFilenamesIn(REMOVAL);
        if (!stagingArea.isEmpty() || !stagingRemoval.isEmpty()) {
            System.out.println("You have uncommitted changes.");
            failureCheck = true;
            return failureCheck;
        }
        File givenBranch = Utils.join(BRANCHES, branch);
        if (!givenBranch.exists()) {
            System.out.println("A branch with that name does not exist.");
            failureCheck = true;
            return failureCheck;
        }
        String currBranch = Utils.readContentsAsString(HEAD);
        if (currBranch.equals(branch)) {
            System.out.println("Cannot merge a branch with itself.");
            Commit currCommit = getCurrentCommit();
            String givenBranchID = Utils.readContentsAsString
                    (Utils.join(BRANCHES, branch));
            Commit givenCommit = Utils.readObject
                    (Utils.join(COMMITS, givenBranchID), Commit.class);
            mergeCommit(currBranch, branch,
                    currCommit.getID(), givenCommit.getID());
            failureCheck = true;
            return failureCheck;
        }
        List<String> currWorkingFiles = Utils.plainFilenamesIn(CWD);
        Commit currCommit = getCurrentCommit();
        for (String file : currWorkingFiles) {
            File curr = Utils.join(CWD, file);
            byte[] content = Utils.readContents(curr);
            String iD = Utils.sha1(content);
            HashMap<String, String> currBlobs = currCommit.getBlobs();
            String currContent = currBlobs.get(file);
            if (!iD.equals(currContent)) {
                System.out.println("There is an untracked file in the way;"
                        + " " + "delete it, or add and commit it first.");
                failureCheck = true;
                return failureCheck;
            }
        }
        return failureCheck;
    }

    /** to merge branches.
     * @param branch branch to be merged.
     */
    public static void merge(String branch)
            throws IOException {
        if (mergeFailureCheck(branch)) {
            return;
        }
        String currBranch = Utils.readContentsAsString(HEAD);
        Commit currCommit = getCurrentCommit();
        String givenBranchID = Utils.readContentsAsString
                (Utils.join(BRANCHES, branch));
        Commit givenCommit = Utils.readObject
                (Utils.join(COMMITS, givenBranchID), Commit.class);
        Commit splitPoint = getSplitPoint(branch);
        String splitPointID = "";
        if (splitPoint != null) {
            splitPointID = splitPoint.getID();
        }
        if (splitPointID.equals(givenBranchID)) {
            System.out.println("Given branch is"
                    + "an ancestor of the curren branch.");
            return;
        } else if (splitPointID.equals(currCommit.getID())) {
            String input = "checkout" + branch;
            checkout(input);
            System.out.println("Current branch fast-forwarded");
            return;
        } else {
            HashMap<String, String> givenBlobs = givenCommit.getBlobs();
            HashMap<String, String> currBlobs = currCommit.getBlobs();
            HashMap<String, String> splitBlobs = null;
            if (splitPoint != null) {
                splitBlobs = splitPoint.getBlobs();
            }
            mergeOne(currBlobs, splitBlobs, givenBlobs);
            mergeTwo(currBlobs, splitBlobs, givenBlobs,
                    splitPoint, givenCommit);
            mergeCommit(currBranch, branch,
                    currCommit.getID(), givenCommit.getID());
        }
    }

    /** Helper function to do merge part one.
     * @param currBlobs current blobs
     * @param givenBlobs given blobs
     * @param splitBlobs split blobs
     */
    public static void mergeOne(HashMap<String, String> currBlobs,
                                HashMap<String, String> splitBlobs,
                                HashMap<String, String> givenBlobs)
            throws IOException {
        for (String file : currBlobs.keySet()) {
            String currentContent = currBlobs.get(file);
            String splitPointContent = null;
            if (splitBlobs != null && splitBlobs.containsKey(file)) {
                splitPointContent = splitBlobs.get(file);
            }
            if (givenBlobs == null || !givenBlobs.containsKey(file)) {
                if (splitBlobs == null || !splitBlobs.containsKey(file)) {
                    continue;
                } else if (splitPointContent != null
                        && splitPointContent.equals(currentContent)) {
                    mergeRemove(file);
                } else {
                    mergeConflict(file, currentContent, null);
                }
            }
        }
    }

    /** Helper function to do merge part two.
     * @param currBlobs current blobs
     * @param givenBlobs given blobs
     * @param splitBlobs split blobs
     * @param splitPoint split point
     * @param givenCommit given commit
     */
    public static void mergeTwo(HashMap<String, String> currBlobs,
                                HashMap<String, String> splitBlobs,
                                HashMap<String, String> givenBlobs,
                                Commit splitPoint, Commit givenCommit)
            throws IOException {
        for (String file : givenBlobs.keySet()) {
            String givenContent = givenBlobs.get(file);
            String currContent = null;
            if (currBlobs != null && currBlobs.containsKey(file)) {
                currContent = currBlobs.get(file);
            }
            String splitPointContent = null;
            if (splitBlobs != null && splitBlobs.containsKey(file)) {
                splitPointContent = splitBlobs.get(file);
            }
            if (splitPoint == null
                    || splitPoint.getBlobs() == null
                    || !splitPoint.getBlobs().containsKey(file)
                    || (!givenContent.equals(splitPointContent)
                    && currContent.equals(splitPointContent))) {
                String[] input = new String[4];
                input[0] = "checkout";
                input[1] = givenCommit.getID();
                input[2] = "--";
                input[3] = file;
                checkout(input);
                add(file);
            } else if ((!givenContent.equals(currContent)
                    && !givenContent.equals(splitPointContent)
                    && !currContent.equals(splitPointContent))
                    || (!givenContent.equals(splitPointContent)
                    && currContent == null)) {
                mergeConflict(file, currContent, givenContent);
            } else if (givenContent.equals(splitPointContent)) {
                continue;
            }
        }
    }

    /** Helper function to do merge conflict.
     * @param file given file.
     * @param currContent contents of file in current branch.
     * @param givenContent contents of file in given branch.
     */
    public static void mergeConflict(String file,
                                     String currContent, String givenContent)
            throws IOException {
        String content = "";
        content += "<<<<<<< HEAD" + "\n";
        if (currContent != null) {
            content += Utils.readContentsAsString
                    (Utils.join(BLOBS, currContent)) + "\n";
        }
        content += "=======" + "\n";
        if (givenContent != null) {
            content += Utils.readContentsAsString
                    (Utils.join(BLOBS, givenContent)) + "\n";
        }
        content += ">>>>>>>";
        Utils.writeContents(Utils.join(CWD, file), content.getBytes());
        add(file);
        System.out.println("Encountered a merge conflict.");
    }

    /** Helper function to do merge, remove, and then untrack.
     * @param file given file.
     */
    public static void mergeRemove(String file) throws IOException {
        String currContent = Utils.readContentsAsString
                (Utils.join(CWD, file));
        Utils.join(CWD, file).delete();
        File removedFile = Utils.join(REMOVAL, file);
        removedFile.createNewFile();
        Utils.writeContents(removedFile, currContent.getBytes());
    }

    /** Helper function to do merge commit.
     * @param curr current branch.
     * @param given given branch.
     * @param firstParent first parent.
     * @param secondParent second parent.
     */
    public static void mergeCommit(String curr, String given,
                                   String firstParent, String secondParent)
            throws IOException {
        List<String> allWorkingFiles = Utils.plainFilenamesIn(CWD);
        HashMap<String, String> newBlobs = new HashMap<>();
        for (String file : allWorkingFiles) {
            File currFile = Utils.join(CWD, file);
            String contentID = Utils.sha1(Utils.readContents(currFile));
            newBlobs.put(file, contentID);
        }
        List<String> allStagedFiles = Utils.plainFilenamesIn(STAGING);
        for (String file : allStagedFiles) {
            Utils.join(STAGING, file).delete();
        }
        List<String> allRemovedFiles = Utils.plainFilenamesIn(REMOVAL);
        for (String file: allRemovedFiles) {
            Utils.join(REMOVAL, file).delete();
        }
        String message = "Merged" + " " + given
                + " "  + "into" + " " + curr + ".";
        Commit newCommit = new Commit(message, firstParent, newBlobs);
        newCommit.setFirstParent(firstParent);
        newCommit.setSecondParent(secondParent);
        String newCommitID = newCommit.getID();
        File addedCommit = Utils.join(COMMITS, newCommitID);
        addedCommit.createNewFile();
        Utils.writeObject(addedCommit, newCommit);
        Utils.writeContents(Utils.join(BRANCHES, curr), newCommitID);
    }
}
