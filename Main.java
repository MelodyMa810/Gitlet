package gitlet;

import java.io.IOException;

/** Driver class for Gitlet, the tiny stupid version-control system.
 *  @author Melody Ma
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> ....
     *  java gitlet.Main add hello.txt*/
    public static void main(String... args) throws IOException {
        SomeObj bloop = new SomeObj();
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            return;
        }

        if (args[0].equals("init")) {
            bloop.init();
        } else if (args[0].equals("add")) {
            bloop.add(args[1]);
        } else if (args[0].equals("commit")) {
            bloop.commit(args[1]);
        } else if (args[0].equals("checkout")) {
            bloop.checkout(args);
        } else if (args[0].equals("rm")) {
            bloop.rm(args[1]);
        } else if (args[0].equals("log")) {
            bloop.log();
        } else if (args[0].equals("global-log")) {
            bloop.globalLog();
        } else if (args[0].equals("find")) {
            bloop.find(args[1]);
        } else if (args[0].equals("branch")) {
            bloop.branch(args[1]);
        } else if (args[0].equals("rm-branch")) {
            bloop.rmBranch(args[1]);
        } else if (args[0].equals("reset")) {
            bloop.reset(args[1]);
        } else if (args[0].equals("status")) {
            bloop.status();
        } else if (args[0].equals("merge")) {
            bloop.merge(args[1]);
        } else {
            System.out.println("No command with that name exists.");
        }
    }
}
