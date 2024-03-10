package sample;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Server{
    private static HashMap<String, String> usersPages = new HashMap<>();
    private static HashMap<String, MyPage> getPageWithPassword = new HashMap<>();
    private static HashSet<String> setOfLogins = new HashSet<>();
    private static int num = 0;


    public static void main(String[] args) throws IOException {

        new Thread(() -> {
            try(ServerSocket serverSocket = new ServerSocket(9090)){

                for(;;) {
                    Socket socket = serverSocket.accept();
                    System.out.println("Client " + (++num) + " accept.");

                    new Thread(() -> {
                        try(
                        DataOutputStream writer = new DataOutputStream(socket.getOutputStream());
                        DataInputStream reader = new DataInputStream(socket.getInputStream())){

                        try { mainPage(reader, writer); }
                        catch (IOException e) { e.printStackTrace(); }

                        }catch (IOException b){b.printStackTrace();}
                    }).start();
                }
            }catch (IOException e) {e.printStackTrace();}
        }).start();
    }

    private static void mainPage(DataInputStream reader, DataOutputStream writer) throws IOException{
        int n = reader.readInt();
        if (n == 1) checkLoginPassword(reader, writer);
        else checkRegistration(reader, writer);
    }

    private static void interfacePage(DataInputStream reader, DataOutputStream writer, String login) throws IOException{
        writer.writeUTF(getPageWithPassword.get(usersPages.get(login)).getLogin());
        int n = reader.readInt();

        switch (n){
            case 1 ->  myPageInterFace(reader, writer, login);
            case 2 ->  mySubscribesPage(reader, writer, login);
            case 3 ->  myBlockingUsersPage(reader, writer, login);
            case 4 ->  searchUsersToSubs(reader, writer, login);
            case 5 ->  interestingPage(reader, writer, login);
            case 6 ->  writer.writeUTF("See you soon");
        }

    }

    private static void interestingPage(DataInputStream reader, DataOutputStream writer, String email) throws IOException {
        ArrayList<Integer> idxInteresting = new ArrayList<>();
        HashMap<Integer, String> allInteresting = new HashMap<>();
        HashMap<Integer, String> tempLogins = new HashMap<>();
        HashMap<Integer, Integer> allLikes = new HashMap<>();
        HashMap<Integer, LinkedList<String>> allComments = new HashMap<>();
        readPosts(email, allInteresting, tempLogins, allLikes, allComments, idxInteresting);


        int n1 = reader.readInt();
        if(n1 == 1){ idxInteresting = sortByLikesMergeSort( idxInteresting, allLikes); }
        else if(n1 == 2){}
        else{ interfacePage(reader, writer, email);}

        System.out.println(idxInteresting);
        System.out.println(allInteresting);
        System.out.println(allLikes);

        if (allInteresting.isEmpty()) { writer.writeBoolean(true); writer.writeUTF("No posts."); }
        else {
            writer.writeBoolean(false);
            writer.writeInt(allInteresting.size());
            for (int i = 0; i < allInteresting.size(); i++){
                   writer.writeUTF(tempLogins.get(idxInteresting.get(i)));
                   writer.writeUTF(allInteresting.get(idxInteresting.get(i)));
                   writer.writeInt(allLikes.get(idxInteresting.get(i)));

                   if(allComments.get(idxInteresting.get(i)).isEmpty()){ writer.writeBoolean(true); writer.writeUTF("No comments"); }
                   else {
                       writer.writeBoolean(false);
                       writer.writeInt(allComments.get(idxInteresting.get(i)).size());
                       for (int j = 0; j < allComments.get(idxInteresting.get(i)).size(); j++) writer.writeUTF(allComments.get(idxInteresting.get(i)).get(j));
                   }

                   int n = reader.readInt();
                   switch (n){
                       case 0 ->  interfacePage(reader, writer, email);
                       case 1 -> {
                           if(!getPageWithPassword.get(usersPages.get(email)).getLikedPosts().
                                   contains(allInteresting.get(idxInteresting.get(i)))) {
                               for (int j = 0; j < getPageWithPassword.get(usersPages.get(tempLogins.get(idxInteresting.get(i)))).getPosts().size(); j++) {
                                   if (getPageWithPassword.get(usersPages.get(tempLogins.get(idxInteresting.get(i)))).getPosts().get(j).
                                           equals(allInteresting.get(idxInteresting.get(i)))) {
                                       getPageWithPassword.get(usersPages.get(tempLogins.get(idxInteresting.get(i)))).getPostLikes().merge(j, 1, Integer::sum);
                                       getPageWithPassword.get(usersPages.get(email)).getLikedPosts().add(allInteresting.get(idxInteresting.get(i)));
                                       writer.writeUTF("You like this post.\n");
                                       break;
                                   }
                               }
                           }
                           else { writer.writeUTF("You already liked this post.\n"); }
                       }

                       case 2 -> {
                           String comment = reader.readUTF();
                           for (int j = 0; j < getPageWithPassword.get(usersPages.get(tempLogins.get(idxInteresting.get(i)))).getPosts().size(); j++) {
                               if (getPageWithPassword.get(usersPages.get(tempLogins.get(idxInteresting.get(i)))).getPosts().get(j).
                                       equals(allInteresting.get(idxInteresting.get(i)))) {
                                   getPageWithPassword.get(usersPages.get(tempLogins.get(idxInteresting.get(i)))).getPostComments().get(j).add(comment);
                                   writer.writeUTF("You add your comment\n");
                                   break;
                               }
                           }
                       }
                       case 3 ->  writer.writeUTF("\n");
                   }

            }
        }

        allInteresting.clear();
        tempLogins.clear();
        allLikes.clear();
        allComments.clear();
        interfacePage(reader, writer, email);
    }

    private static ArrayList<Integer> sortByLikesMergeSort(ArrayList<Integer> idxInteresting, HashMap<Integer, Integer> allLikes){
        if(allLikes.isEmpty()) return idxInteresting;
        if(idxInteresting.size() == 1) return idxInteresting;
        int mid = idxInteresting.size() / 2;


        ArrayList<Integer> left = new ArrayList<>(mid);
        for (int i = 0; i < mid; i++) left.add(idxInteresting.get(i));

        ArrayList<Integer> right = new ArrayList<>(idxInteresting.size() - mid);
        for (int i = mid; i < idxInteresting.size(); i++) right.add(idxInteresting.get(i));


        left = sortByLikesMergeSort(left, allLikes);
        right = sortByLikesMergeSort(right, allLikes);
        idxInteresting = merge(left, right, allLikes);

        return idxInteresting;
    }

    private static ArrayList<Integer> merge(ArrayList<Integer> left, ArrayList<Integer> right, HashMap<Integer, Integer> allLikes){
        int l = 0, r = 0;
        ArrayList<Integer> temp = new ArrayList<>();

        while(l < left.size() && r < right.size()){
            if(allLikes.get(left.get(l)) < allLikes.get(right.get(r))) {  temp.add(left.get(l)); l++; }
            else { temp.add(right.get(r)); r++; }
        }

        for (int i = l; i < left.size(); i++) temp.add(left.get(i));
        for (int i = r; i < right.size(); i++) temp.add(right.get(i));

        return temp;
    }

    private static void sortByLikesQuickSort(int l, int r, ArrayList<Integer> idxInteresting, HashMap<Integer, Integer> allLikes){
        if(l >= r){ return; }
        int pi = partition(l, r, idxInteresting, allLikes);

        sortByLikesQuickSort(l, pi - 1, idxInteresting, allLikes);
        sortByLikesQuickSort(pi + 1, r, idxInteresting, allLikes);
    }

    private static int partition(int l, int r, ArrayList<Integer> idxInteresting, HashMap<Integer, Integer> allLikes){
        int pivot = allLikes.get(idxInteresting.get(r));
        int ptr = l - 1;

        for (int i = l; i < r; i++)
            if(pivot > allLikes.get(idxInteresting.get(i))){ ptr++; swap(idxInteresting, i, ptr); }

        swap(idxInteresting, r, ptr + 1);
        return ptr + 1;
    }

    private static void swap(ArrayList<Integer> idxInteresting, int l, int r){
        int temp = idxInteresting.get(l);
        idxInteresting.set(l, idxInteresting.get(r));
        idxInteresting.set(r, temp);
    }



    private static void readPosts(String email, HashMap<Integer, String> allInteresting, HashMap<Integer, String> tempLogins, HashMap<Integer, Integer> allLikes,
                                   HashMap<Integer, LinkedList<String>> allComments, ArrayList<Integer> idxInteresting){
        int idx = 0;
        for(String e : setOfLogins){
            if(e.equals(email) || getPageWithPassword.get(usersPages.get(email)).getBlockingUsers().contains(e)) { }
            else{
                LinkedList<String> temp = getPageWithPassword.get(usersPages.get(e)).getPosts();
                for (int i = 0; i < temp.size(); i++){
                    idxInteresting.add(idx);
                    allInteresting.put(idx, temp.get(i));
                    tempLogins.put(idx, e);
                    allLikes.put(idx, getPageWithPassword.get(usersPages.get(e)).getPostLikes().get(i));
                    allComments.put(idx,  getPageWithPassword.get(usersPages.get(e)).getPostComments().get(i));
                    idx++;
                }
            }
        }

    }

    private static void myBlockingUsersPage(DataInputStream reader, DataOutputStream writer, String email) throws IOException{
        MyPage my = getPageWithPassword.get(usersPages.get(email));
        if(my.getBlockingUsers().isEmpty()){writer.writeBoolean(false); writer.writeUTF("You don't have blocked users."); }
        else{
            writer.writeBoolean(true);
            writer.writeInt(my.getBlockingUsers().size());
            LinkedList<String> listOfLogins = new LinkedList<>();
            for (String e : my.getBlockingUsers()){ writer.writeUTF(e); listOfLogins.add(e); }

            int n = reader.readInt();
            if(n == 0) interfacePage(reader, writer, email);
            else unBlockPage(reader, writer, email, listOfLogins, n-1);
        }

        interfacePage(reader, writer, email);
    }

    private static void unBlockPage(DataInputStream reader, DataOutputStream writer, String email, LinkedList<String> listOfLogins, int n) throws IOException{
        MyPage myPage = getPageWithPassword.get(usersPages.get(listOfLogins.get(n)));
        getInfoAboutUser(writer, myPage);

        int n2 = reader.readInt();
        MyPage my = getPageWithPassword.get(usersPages.get(email));
        if(n2 == 1){
            my.getBlockingUsers().remove(listOfLogins.get(n));
            my.getFriends().add(listOfLogins.get(n));
            writer.writeUTF("You unblock " + listOfLogins.get(n) + "."); }
        else { myBlockingUsersPage(reader, writer, email); }
    }



    private static void searchUsersToSubs(DataInputStream reader, DataOutputStream writer, String email) throws IOException {
        HashSet<String> loginsDiff = new HashSet<>(setOfLogins);
        loginsDiff.removeAll(getPageWithPassword.get(usersPages.get(email)).getBlockingUsers());
        loginsDiff.removeAll(getPageWithPassword.get(usersPages.get(email)).getFriends());

        if(loginsDiff.size() == 1) { writer.writeBoolean(false); writer.writeUTF("No users."); }
        else{
            writer.writeBoolean(true);
            writer.writeInt(loginsDiff.size());
            ArrayList<String> listOfLogins = new ArrayList<>();

            for (String e : loginsDiff){
                if(e.equals(email)){ }
                else{ writer.writeUTF(e); listOfLogins.add(e); }
            }

            int n = reader.readInt();
            if(n == 0) interfacePage(reader, writer, email);
            else{ subsPage(reader, writer, email, listOfLogins, n-1); }
        }
        interfacePage(reader, writer, email);
    }

    private static void subsPage(DataInputStream reader, DataOutputStream writer, String email, ArrayList<String> listOfLogins, int n) throws IOException{
        MyPage myPage = getPageWithPassword.get(usersPages.get(listOfLogins.get(n)));
        getInfoAboutUser(writer, myPage);

        int n2 = reader.readInt();
        MyPage my = getPageWithPassword.get(usersPages.get(email));
        if(n2 == 1){ my.getFriends().add(listOfLogins.get(n)); writer.writeUTF("You subscribes to " + listOfLogins.get(n) + ".");}
        else { searchUsersToSubs(reader, writer, email); }
    }

    private static void mySubscribesPage(DataInputStream reader, DataOutputStream writer, String email) throws IOException{
        MyPage my = getPageWithPassword.get(usersPages.get(email));
        if (my.getFriends().isEmpty()) {
            writer.writeBoolean(false);
            writer.writeUTF("You don't have subscribes");
        }
        else{
            writer.writeBoolean(true);

            int n = reader.readInt();
            if(n == 1) {
                writer.writeInt(my.getFriends().size());
                LinkedList<String> listOfLogins = new LinkedList<>();
                for (String e : my.getFriends()) {
                    writer.writeUTF(e);
                    listOfLogins.add(e);
                }

                int n1 = reader.readInt();
                if (n1 == 0) interfacePage(reader, writer, email);
                else unSubPage(reader, writer, email, listOfLogins, n1 - 1);
            }
            else if(n == 2){

                String maxLogin = "";
                int maxSubs = 0;
                HashSet<String> temp = new HashSet<>(my.getFriends());


                for (String e : my.getFriends()){
                    temp.retainAll(getPageWithPassword.get(usersPages.get(e)).getFriends());
                    if(temp.size() > maxSubs){ maxSubs = temp.size(); maxLogin = e; }
                    temp = new HashSet<>(my.getFriends());
                }

                if(maxSubs != 0){ writer.writeInt(maxSubs); writer.writeUTF(maxLogin); }
                else{ writer.writeInt(0); writer.writeUTF("You don't hava common subscribers with your subscribes"); }
            }
            else{ interfacePage(reader, writer, email); }
        }

        interfacePage(reader, writer, email);
    }

    private static void unSubPage(DataInputStream reader, DataOutputStream writer, String email, LinkedList<String> listOfLogins, int n) throws IOException{
        MyPage myPage = getPageWithPassword.get(usersPages.get(listOfLogins.get(n)));
        getInfoAboutUser(writer, myPage);

        int n2 = reader.readInt();
        MyPage my = getPageWithPassword.get(usersPages.get(email));
        if(n2 == 1){ my.getFriends().remove(listOfLogins.get(n)); writer.writeUTF("You unsubscribes from " + listOfLogins.get(n) + "."); }
        else if(n2 == 2){ my.getFriends().remove(listOfLogins.get(n)); my.getBlockingUsers().add(listOfLogins.get(n)); writer.writeUTF("You block " + listOfLogins.get(n) + "."); }
        else { mySubscribesPage(reader, writer, email); }
        //System.out.println(my.getFriends());
    }

    private static void getInfoAboutUser(DataOutputStream writer, MyPage myPage) throws IOException{
        writer.writeUTF(myPage.getName());
        writer.writeUTF(myPage.getSurname());
        writer.writeInt(myPage.getAge());
        writer.writeUTF(myPage.getGender());
        writer.writeUTF(myPage.getPhoneNumber());
    }



    private static void myPageInterFace(DataInputStream reader, DataOutputStream writer, String email) throws IOException{
        MyPage myPage = getPageWithPassword.get(usersPages.get(email));
        getInfoAboutUser(writer, myPage);


        int n = reader.readInt();
        switch (n){
            case 1 -> {
                writer.writeBoolean(myPage.getPosts().isEmpty());
                if(myPage.getPosts().isEmpty()){ writer.writeUTF("You don't have posts"); }
                else{
                    int length = myPage.getPosts().size();
                    writer.writeInt(length);
                    for (int i = 0; i < length; i++){
                        writer.writeUTF(myPage.getPosts().get(i));
                        writer.writeInt(myPage.getPostLikes().get(i));

                        writer.writeBoolean(myPage.getPostComments().get(i).isEmpty());
                        if(myPage.getPostComments().get(i).isEmpty()){ writer.writeUTF("Post doesn't have comments"); }
                        else{
                            int length2 = myPage.getPostComments().get(i).size();
                            writer.writeInt(length2);
                            for (int j = 0; j < length2; j++) writer.writeUTF(myPage.getPostComments().get(i).get(j));
                        }
                    }

                }

            }
            case 2 -> {
                int k = reader.readInt();
                if(k > myPage.getPosts().size()){ writer.writeBoolean(false); }
                else{
                    writer.writeBoolean(true);
                    myPage.pushPosts(k);

                    int i = 0;
                    while(i < k){
                        writer.writeUTF(myPage.getPosts().get(myPage.getMaxH().peek()[0]));
                        writer.writeInt(myPage.getMaxH().peek()[1]);
                        myPage.getMaxH().poll();
                        i++;
                    }
                }

            }
            case 3 -> {
                myPage.getPosts().add(reader.readUTF());
                myPage.getPostLikes().put(myPage.getPosts().size() - 1, (int)(0 + Math.random() * 10));
                myPage.getPostComments().put(myPage.getPosts().size() - 1, new LinkedList<>());
            }
            case 4 -> interfacePage(reader, writer, email);
        }

        interfacePage(reader, writer, email);
    }


    private static void checkRegistration(DataInputStream reader, DataOutputStream writer) throws IOException {
        String name;
        String surname;
        int age = 0;
        String email = "";
        String password = "";
        String gender;
        String phoneNumber;

        name = validation(reader, writer, false, "([a-zA-Z',.-]+( [a-zA-Z',.-]+)*){3,30}");

        surname = validation(reader, writer, false, "([a-zA-Z',.-]+( [a-zA-Z',.-]+)*){3,30}");

        boolean r = false;
        while(!r){
            age = reader.readInt();
            if(age >= 18 && age <= 150){
                r = true;
                writer.writeBoolean(r);
                writer.writeUTF("Correct input");
            }else{
                writer.writeBoolean(r);
                if(age < 18) writer.writeUTF("Invalid age(too young)");
                else writer.writeUTF("Out of the limitation");
            }
        }

        phoneNumber = validation(reader, writer, false, "^?[1-9][0-9]{7,14}$");

        int n = reader.readInt();
        if(n == 1){ gender = "Male"; }
        else if(n == 2) { gender = "Female"; }
        else{ gender = "None"; }
        writer.writeUTF("Correct input");

       // email = validation(reader, writer, false, "[A-Za-z0-9+_.-]+@(.+)$");
        r = false;
        while (!r) {
            email = reader.readUTF();
            if(setOfLogins.contains(email)){
                writer.writeBoolean(r);
                writer.writeUTF("This email is exist. Please write another one.");
            }
            else if (email.matches("[A-Za-z0-9+_.-]{7,30}+@(.+)$")) {
                r = true;
                writer.writeBoolean(r);
                writer.writeUTF("Correct input");
            } else {
                writer.writeBoolean(r);
                writer.writeUTF("Enter again invalid input");
            }
        }

        r = false;
        while (!r){
            password = reader.readUTF();
            if(password.matches("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&-+=()])(?=\\S+$).{8,20}$")){
                r = true;
                writer.writeBoolean(r);
                writer.writeUTF("Account is created. Thank you!!");
            }else{
                writer.writeBoolean(r);
                writer.writeUTF("Enter again invalid input");
            }
        }

        getPageWithPassword.put(password, new MyPage(name, surname, phoneNumber,
                age, gender, email.toLowerCase(Locale.ROOT), password));
        usersPages.put(email.toLowerCase(Locale.ROOT), password);
        setOfLogins.add(email.toLowerCase(Locale.ROOT));

        System.out.println(name + " " + surname + " " + age + " " + gender + " " +
               phoneNumber +  " " + email + " " + password);
        mainPage(reader, writer);
    }

    private static String validation(DataInputStream reader, DataOutputStream writer, boolean r, String regex) throws IOException {
        String name = "";
        while (!r) {
            name = reader.readUTF();
            if (name.matches(regex)) {
                r = true;
                writer.writeBoolean(r);
                writer.writeUTF("Correct input");
            } else {
                writer.writeBoolean(r);
                writer.writeUTF("Enter again invalid input");
            }
        }
        return  name;
    }

    private static void checkLoginPassword(DataInputStream reader, DataOutputStream writer) throws IOException {
        String login = "";
        boolean r = false;
        while (!r){
            login = (reader.readUTF()+"@sky.net").toLowerCase(Locale.ROOT);
            if(login.equals("0@sky.net")){ r = true; writer.writeBoolean(r); writer.writeUTF("Exit."); mainPage(reader, writer);}
            else if(setOfLogins.contains(login)){
                r = true;
                writer.writeBoolean(r);
                writer.writeUTF("Correct email, enter password.");
            }else{
                writer.writeBoolean(r);
                writer.writeUTF("Incorrect(there is no such login). If you want exit enter '0'.");
            }
        }


        String password;
        r = false;
        while(!r){
            password = reader.readUTF();
            if(password.equals("0")){ r = true; writer.writeBoolean(r); writer.writeUTF("Exit."); mainPage(reader, writer);}
            else if(password.equalsIgnoreCase(usersPages.get(login))){
                r = true;
                writer.writeBoolean(r);
                writer.writeUTF("Welcome to Skynet " + getPageWithPassword.get(usersPages.get(login)).getName() +".");
            }else{
                writer.writeBoolean(r);
                writer.writeUTF("Incorrect(there is no such password). If you want exit enter '0'.");
            }
        }
        interfacePage(reader, writer, login);
    }

/*
    private static void dis(Queue<Integer[]> b){
        Queue<Integer[]> a = b;
        while (!a.isEmpty()){
            System.out.println(a.peek()[0] + " " +a.peek()[1]);
            a.poll();
        }
    }*/

}
