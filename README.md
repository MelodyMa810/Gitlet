# Gitlet
Gitlet is a mini version of the version-control system Git that stores collections of files.

The important structures of Git are **blobs** - contents of files, **trees** - directory structures mapping specific file names to blobs, and **commits** - snapshots of all working files. Each blob and commit have a unique ID, created by SHA-1 hash functions.

To achieve the function of version-control, below is a list of commands I implemented:<br>
```java gitlet.Main init```<br>
```java gitlet.Main add [file name]```<br>
```java gitlet.Main commit [message]```<br>
```java gitlet.Main rm [file name]```<br>
```java gitlet.Main log```<br>
```java gitlet.Main global-log```<br>
```java gitlet.Main find [commit message]```<br>
```java gitlet.Main status```<br>
```java gitlet.Main checkout```<br>
```java gitlet.Main branch [branch name]```<br>
```java gitlet.Main rm-branch [branch name]```<br>
```java gitlet.Main reset [commit id]```<br>
```java gitlet.Main merge [branch name]```<br>

For the detailed spec of this project: https://inst.eecs.berkeley.edu/~cs61b/fa21/materials/proj/proj3/index.html.
