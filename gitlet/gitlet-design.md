# Gitlet Design Document

**Name**: Melody Ma

## Classes and Data Structures

### SomeObj

#### init
* Create staging area, commits, blobs, and branches as directories within .gitlet directory
* Within "branches" directory, each file represents a branch, with the content of the file being the commit ID the branch is pointing to

#### add
* Add the file to the staging area

#### commit
* Clone the parent commit
* Update information from the parent commit
* Make a new commit with updated information and parent being the last commit
* Update master and head pointers

#### checkout
* Change the current working file to the file/commit indicated

#### log
* Print out the history of a commit

### Commit

#### Instance Variables
* Message - contains the message of a commit.
* Timestamp - time at which a commit was created, assigned by the constructor.
* Prent - the parent commit of a commit object.

## Algorithms



## Persistence

