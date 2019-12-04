# File-Storage
Using the programming techniques related to socket and thread programming to build a File Storage server and the corresponding client in java.

# Fetures
1. Creating a new user account, where:

  a. The user should provide the email address and account password , in order to create the account. The server shouldn’t accept duplicate emails.
  b. A unique account ID must be generated for this user by the server, the server should create a root directory named with the corresponding unique user ID on the server side.
  c. Each root directory for each client contains a home directory to hold this client files and directories. Note that the client top level directory to be viewed is his home directory.
  
2. Login using the user email and password. Note: handling invalid email or account password.

3. Support the change directory command (cd) , where:
  a. The client should be able to navigate within his directories.
  b. The the top level directory that the client could navigate to is his home directory.
  
4. Support the print working directory command (pwd) , where:
  a. The client should be able to check the current directory path.
  b. Note: The top level for the current directory path is the client’s home directory. so hiding the server main storage details.
  
5. Support the list command (ls) , where: The client should be able to list all files and directories in the current working directory.

6. Support the make and remove directory commands (mkdir and rmdir) , where:
 a. The mkdir command enables the user to create new directories to current working directory.
 b. The rmdir command enables the user to remove directories from current working directory.
 c. Note: System not accept duplicate name for files or directories within the same parent directory.
7. Support move,copy,rename and delete file commands (mv,cp,rnm and rm) , where:
  a. The client should be able to manipulate his files moving(mv) or copying(cp) a file to another directory, renaming(rnm) a file within the current directory, or deleting(rm) a file from the current directory.
  b. Note: System not accept duplicate name for files or directories within the same parent directory.
8. Support the upload and download file commands (upload and download) , where:
  a. The user could upload a file from local storage system to the current working directory at the server side.
  b. The user could download from the current working directory at the server side to the local storage system.
  c. Note: System not accept duplicate name for files or directories within the same parent directory.
  
#  Additions
1. Login information saved in database(mysql).
2. Password is hashed using Bycrypt.
