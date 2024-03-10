package sample;

import java.util.*;

public class MyPage {
    private String name;
    private String surname;
    private String phoneNumber;
    private int age;
    private String gender;
    private String login;
    private String password;
    private HashSet<String> likedPosts = new HashSet<>();
    private LinkedList<String> posts = new LinkedList<>();
    private HashMap<Integer, LinkedList<String>> postComments = new HashMap<>();
    private HashMap<Integer, Integer> postLikes = new HashMap<>();
 /*   private Queue<Integer[]> maxHeap = new PriorityQueue<>((o1, o2) -> {
        if(o1[1] < o2[1]) return 1;
        if(o1[1] > o2[1]) return -1;
        else return 0;
    });*/
    private PriorityQueue maxHeap = new PriorityQueue();
    private Queue<Integer[]> maxH = new LinkedList<>();
    private HashSet<String> friends = new HashSet<>();
    private HashSet<String> blockingUsers = new HashSet<>();

    public Queue<Integer[]> getMaxH() {
        return maxH;
    }

    public HashMap<Integer, LinkedList<String>> getPostComments() {
        return postComments;
    }

    public HashSet<String> getLikedPosts() {
        return likedPosts;
    }

    public HashSet<String> getBlockingUsers() {
        return blockingUsers;
    }

    public HashSet<String> getFriends() {
        return friends;
    }


    public void pushPosts(int k){
        maxHeap.clear();
        maxH.clear();
        HashMap<Integer, Integer> idxMap = new HashMap<>();
        for (int e : postLikes.keySet()) { maxHeap.add(postLikes.get(e)); idxMap.put(postLikes.get(e), e); }


        for (int i = 0; i < k; i++){
            int temp = (int)maxHeap.peek();
            maxH.add(new Integer[]{idxMap.get(temp), temp});
            maxHeap.poll();
        }
    }



    /*public Queue<Integer[]> getMaxHeap() {
        return maxHeap;
    }*/

    /* public void pushPosts(){
         maxHeap.clear();
         for (int e : postLikes.keySet()){  maxHeap.add(new Integer[]{e, postLikes.get(e)}); };
     }*/
    public LinkedList<String> getPosts() {
        return posts;
    }

    public HashMap<Integer, Integer> getPostLikes() {
        return postLikes;
    }

    public String getName() {
        return name;
    }

    public String getSurname() {
        return surname;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public int getAge() {
        return age;
    }

    public String getGender() {
        return gender;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    public MyPage(String name, String surname, String phoneNumber, int age,
                  String gender, String login, String password){
        this.age = age;
        this.gender = gender;
        this.login = login;
        this.name = name;
        this.surname = surname;
        this.phoneNumber = phoneNumber;
        this.password = password;
    }

}
