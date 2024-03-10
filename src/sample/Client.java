package sample;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.InputMismatchException;
import java.util.Locale;
import java.util.Scanner;

public class Client {
    private DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
    private Date date = new Date();
    private final Scanner scanner = new Scanner(System.in);
    private DataInputStream reader;
    private DataOutputStream writer;
    private Socket socket;
    private String email = "";

    public Client() throws IOException { connectToTheServer(); }

    private  void connectToTheServer() throws IOException {
        socket = new Socket("localhost", 9090);
        reader = new DataInputStream(socket.getInputStream());
        writer = new DataOutputStream(socket.getOutputStream());
    }
    public void start() throws IOException {this.mainPage();}


    private void mainPage() throws IOException{
        System.out.println("==============================================================\n");
        System.out.println("Welcome to SKYNET!!\n");
        System.out.println("1. I already have an account");
        System.out.println("2. Register");

        System.out.print("Choose one: ");
        try{
            int n = scanner.nextInt();
        while(n != 1 && n != 2){ System.out.print("Choose one: "); n = scanner.nextInt(); }
        writer.writeInt(n);

        if(n == 1) loginPasswordPage();
        else registrationPage();
        }catch (InputMismatchException e){ System.out.println("Incorrect format of input"); }
    }

    public void interfaceOfPage() throws IOException{
        System.out.println("\n==============================================================");
            System.out.println("\n                  Account: " + reader.readUTF() + "\n" +
                               "                  Date: " + dateFormat.format(date));

            System.out.println("1. My Page");
            System.out.println("2. My subscribes");
            System.out.println("3. Blocking users");
            System.out.println("4. Search users");
            System.out.println("5. Interesting");
            System.out.println("6. Log out\n");

            int n = 0;
            while(n != 1 && n != 2 && n != 3 && n != 4 && n != 5 && n != 6){ System.out.print("Choose one: "); n = scanner.nextInt(); }
            writer.writeInt(n);

            switch (n) {
                case 1 ->  myPageInterFace();
                case 2 ->  mySubscribesPage();
                case 3 ->  myBlockingUsersPage();
                case 4 ->  searchUsersToSubs();
                case 5 ->  interestingPage();
                case 6 -> { System.out.println(reader.readUTF()); System.exit(0); }
            }
    }

    private void interestingPage() throws IOException{
        System.out.println("\n==============================================================");
        System.out.println("1. Sorting posts by number of likes");
        System.out.println("2. Not sorting posts by number of likes");
        System.out.println("3. Exit");

        int n = Integer.MAX_VALUE;
        while (n != 1 && n != 2 && n != 3){ System.out.print("\nChoose one: "); n = scanner.nextInt(); }
        writer.writeInt(n);

        if(n == 3) interfaceOfPage();

        System.out.println("\n####### INTERESTING #######\n");


        boolean check = reader.readBoolean();
        if(check){ System.out.println(reader.readUTF()); }
        else{
            int length = reader.readInt();

            for (int i = 0; i < length; i++){
                System.out.println(reader.readUTF().toUpperCase(Locale.ROOT) + ":");
                System.out.println(reader.readUTF());
                System.out.println("-------------------------------------------");
                System.out.println("Likes: " + reader.readInt());

                boolean r = reader.readBoolean();
                if(r) { System.out.println(reader.readUTF()); }
                else{
                    int length2 = reader.readInt();
                    System.out.println("Comment of the Post: ");
                    for (int j = 0; j < length2; j++)
                        System.out.println("*** " + reader.readUTF() + " ***");

                    System.out.println();
                }
                likeCommentOrNext();
            }
        }
        interfaceOfPage();
    }

    private void likeCommentOrNext() throws IOException {
        System.out.print("\n1.Like ");
        System.out.print(" 2.Comment ");
        System.out.println(" 3.Next");

        int n = Integer.MAX_VALUE;
        while (n != 1 && n != 2 && n != 3 && n != 0){ System.out.print("\nChoose one(to exit type '0'): "); n = scanner.nextInt(); }
        writer.writeInt(n);

        switch (n){
            case 0 ->  interfaceOfPage();
            case 1 ->  System.out.println(reader.readUTF());
            case 2 -> {
                scanner.nextLine();
                System.out.print("\nWrite your comment: ");
                writer.writeUTF(scanner.nextLine());
                System.out.println(reader.readUTF()); }
            case 3 -> System.out.println(reader.readUTF());
        }

    }

    private void myBlockingUsersPage() throws IOException{
        System.out.println("\n==============================================================");
        System.out.println("\nYour blocking users: ");

        boolean check = reader.readBoolean();

        if(!check){ System.out.println(reader.readUTF()); }
        else{
            int length = reader.readInt();
            for (int i = 0; i < length; i++)
                System.out.println((i+1) + ". " + reader.readUTF());

            System.out.print("\nChoose one(to exit type '0'): ");
            int n = scanner.nextInt();
            while(n > length && n != 0){ System.out.print("\nChoose one(to exit type '0'): "); n = scanner.nextInt(); }

            writer.writeInt(n);
            if(n == 0) interfaceOfPage();
            else{ unBlockPage(); }
        }

        this.interfaceOfPage();
    }

    private void unBlockPage() throws IOException{
        System.out.println("\nName: " + reader.readUTF() + "\n" +
                "Surname: " + reader.readUTF() + "\n" +
                "Age: " + reader.readInt() + "\n" +
                "Gender: " + reader.readUTF() + "\n" +
                "PhoneNumber: " + reader.readUTF() + "\n");

        System.out.print("1.Unblocking ");
        System.out.println(" 2.Exit");

        int n = 0;
        while(n != 1 && n != 2){ System.out.print("Choose one: "); n = scanner.nextInt(); }
        writer.writeInt(n);

        if(n == 1){ System.out.println(reader.readUTF()); }
        else{ myBlockingUsersPage(); }
    }

    private void searchUsersToSubs() throws IOException{
        System.out.println("\n==============================================================");
        System.out.println("\nUsers: ");

        boolean check = reader.readBoolean();

        if(!check){ System.out.println(reader.readUTF()); }
        else{
            int length = reader.readInt() - 1;
            for(int i = 0; i < length; i++)
                System.out.println((i+1) + ". " + reader.readUTF());

            System.out.print("\nChoose one(to exit type '0'): ");
            int n = scanner.nextInt();
            while(n > length && n != 0){ System.out.print("\nChoose one(to exit type '0'): "); n = scanner.nextInt(); }


            writer.writeInt(n);
            if(n == 0) interfaceOfPage();
            else{ subsPage(); }
        }

        this.interfaceOfPage();
    }

    private void subsPage() throws IOException {
        System.out.println("\nName: " + reader.readUTF() + "\n" +
                "Surname: " + reader.readUTF() + "\n" +
                "Age: " + reader.readInt() + "\n" +
                "Gender: " + reader.readUTF() + "\n" +
                "PhoneNumber: " + reader.readUTF() + "\n");

        System.out.print("1.Subscribe. ");
        System.out.println(" 2.Exit");

        int n = 0;
        while(n != 1 && n != 2){ System.out.print("Choose one: "); n = scanner.nextInt(); }
        writer.writeInt(n);

        if(n == 1){ System.out.println(reader.readUTF()); }
        else{ searchUsersToSubs(); }
    }

    private void mySubscribesPage() throws IOException{
        boolean check = reader.readBoolean();

        if(!check){ System.out.println(reader.readUTF()); }
        else {

            System.out.println("\n==============================================================\n");
            System.out.println("1.My subscribes");
            System.out.println("2.Highest number of common subscribers");
            System.out.println("3.Exit\n");

            int n = 0;
            while (n != 1 && n != 2 && n != 3) {
                System.out.print("Choose one: ");
                n = scanner.nextInt();
            }
            writer.writeInt(n);


            if (n == 1) {
                System.out.println("\nYour subscribes: ");

                int length = reader.readInt();
                for (int i = 0; i < length; i++)
                    System.out.println((i + 1) + ". " + reader.readUTF());

                System.out.print("\nChoose one(to exit type '0'): ");
                int n1 = scanner.nextInt();
                while (n1 > length && n1 != 0) {
                    System.out.print("\nChoose one(to exit type '0'): ");
                    n1 = scanner.nextInt();
                }

                writer.writeInt(n1);
                if (n1 == 0) interfaceOfPage();
                else { unSubsPage(); }
            }
            else if (n == 2) {
                System.out.println("\nHighest number of common subscribers is: " + reader.readInt());
                System.out.println("With: " + reader.readUTF() + "\n");
            }
            else { interfaceOfPage(); }

        }

        this.interfaceOfPage();
    }

    private void unSubsPage() throws IOException {
        System.out.println("\nName: " + reader.readUTF() + "\n" +
                "Surname: " + reader.readUTF() + "\n" +
                "Age: " + reader.readInt() + "\n" +
                "Gender: " + reader.readUTF() + "\n" +
                "PhoneNumber: " + reader.readUTF() + "\n");

        System.out.print("1.Unsubscribe. ");
        System.out.print(" 2.Blocking ");
        System.out.println(" 3.Exit");

        int n = 0;
        while(n != 1 && n != 2 && n != 3){ System.out.print("Choose one: "); n = scanner.nextInt(); }
        writer.writeInt(n);

        if(n == 1){ System.out.println(reader.readUTF()); }
        else if(n == 2){ System.out.println(reader.readUTF()); }
        else{ mySubscribesPage(); }
    }

    private void myPageInterFace() throws IOException{
        System.out.println("\n==============================================================");
        System.out.println("\nName: " + reader.readUTF() + "\n" +
                "Surname: " + reader.readUTF() + "\n" +
                "Age: " + reader.readInt() + "\n" +
                "Gender: " + reader.readUTF() + "\n" +
                "PhoneNumber: " + reader.readUTF() + "\n");

        System.out.println("1. My posts");
        System.out.println("2. My k posts with a maximum number of likes");
        System.out.println("3. Add post");
        System.out.println("4. Exit\n");

        int n = 0;
        while(n != 1 && n != 2 && n != 3 && n != 4){ System.out.print("Choose one: "); n = scanner.nextInt(); }
        writer.writeInt(n);


            switch (n) {
                case 1 -> {
                    boolean check = reader.readBoolean();
                    if (check){ System.out.println(reader.readUTF()); }
                    else {
                        System.out.println();
                        int length = reader.readInt();
                        for (int i = 0; i < length; i++) {
                            System.out.println((i + 1) + ". " + reader.readUTF());
                            System.out.println("-------------------------------------------");
                            System.out.println("Likes: " + reader.readInt());

                            boolean r = reader.readBoolean();
                            if (r) {
                                System.out.println(reader.readUTF());
                            } else {
                                int length2 = reader.readInt();
                                System.out.println("Comment of the Post: ");
                                for (int j = 0; j < length2; j++)
                                    System.out.println("*** " + reader.readUTF() + " ***");
                            }
                            System.out.println();
                        }
                    }
                }
                case 2 -> {
                    System.out.print("\nEnter k: ");
                    int k = scanner.nextInt();
                    writer.writeInt(k);
                    boolean check = reader.readBoolean();

                    System.out.println();
                    if(!check){ System.out.println("You don't have " + k + " posts.");  }
                    else{
                        for (int i = 0; i < k; i++){
                            System.out.println((i + 1) + ". " + reader.readUTF());
                            System.out.println("-------------------------------------------");
                            System.out.println("Likes: " + reader.readInt()+"\n");
                        }
                    }
                }
                case 3 -> {
                    System.out.print("Type post: ");
                    String s = scanner.nextLine();
                    while (s.isEmpty()) {
                        System.out.println("Post cannot be empty!!");
                        s = scanner.nextLine();
                    }
                    writer.writeUTF(s);
                }
                case 4 ->  interfaceOfPage();
            }

            this.interfaceOfPage();
    }


    private void registrationPage() throws IOException {
            try {

                boolean nameBool = false;
                while (!nameBool) {
                    System.out.print("\nEnter your Name: ");
                    writer.writeUTF(scanner.next());
                    nameBool = reader.readBoolean();
                    System.out.println(reader.readUTF());
                }

                boolean surname = false;
                while (!surname) {
                    System.out.print("\nEnter your Surname: ");
                    writer.writeUTF(scanner.next());
                    surname = reader.readBoolean();
                    System.out.println(reader.readUTF());
                }

                boolean age = false;
                while (!age) {
                    System.out.print("\nEnter your Age: ");
                    writer.writeInt(scanner.nextInt());
                    age = reader.readBoolean();
                    System.out.println(reader.readUTF());
                }

                boolean number = false;
                while (!number) {
                    System.out.print("\nEnter your PhoneNumber: ");
                    writer.writeUTF(scanner.next());
                    number = reader.readBoolean();
                    System.out.println(reader.readUTF());
                }


                System.out.println("\nEnter your gender");
                System.out.println("""
                        1.Male
                        2.Female
                        3.None""");
                System.out.print("\nChoose one: ");
                int n = scanner.nextInt();
                while (n != 1 && n != 2 && n != 3) {
                    System.out.println("Invalid number\n");
                    System.out.println("""
                            1.Male
                            2.Female
                            3.None""");
                    System.out.print("\nChoose one: ");
                    n = scanner.nextInt();
                }
                writer.writeInt(n);
                System.out.println(reader.readUTF());

                boolean loginBool = false;
                while (!loginBool) {
                    System.out.print("\nEnter your Login: ");
                    email = scanner.next()+"@sky.net";
                    writer.writeUTF(email);
                    loginBool = reader.readBoolean();
                    System.out.println(reader.readUTF());
                }

                boolean passwordBool = false;
                System.out.println("\n" + """
                        1.It contains at least 8 characters and at most 20 characters.
                        2.It contains at least one digit.
                        3.It contains at least one upper case alphabet.
                        4.It contains at least one lower case alphabet.
                        5.It contains at least one special character which includes !@#$%&*()-+=^.
                        6.It doesn’t contain any white space.
                        """);
                while (!passwordBool) {
                    System.out.print("Enter your password: ");
                    //password = scanner.next();
                    writer.writeUTF(scanner.next());
                    passwordBool = reader.readBoolean();
                    System.out.println(reader.readUTF()+"\n");
                }


            } catch (IOException e) { e.printStackTrace(); }

        this.mainPage();
    }

    private void loginPasswordPage() throws IOException{
        boolean r = false;
        while(!r) { System.out.print("\nEnter login: "); r = lpp(r); if(r) break;}

        r = false;
        while(!r) { System.out.print("\nEnter Password: "); r = lpp(r); if(r) break;}

        interfaceOfPage();
    }

    private boolean lpp(boolean r) throws IOException {
        String s = scanner.next();
        writer.writeUTF(s);
        r = reader.readBoolean();
        System.out.println(reader.readUTF());
        if(s.equals("0")){ System.out.println("\n"); r = true; this.mainPage();}
        return r;
    }

}
