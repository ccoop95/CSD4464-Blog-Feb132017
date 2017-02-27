/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package buildit;

import java.util.ArrayList;
import java.util.List;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
/**
 *
 * @author c0659824
 */
@ApplicationScoped
@Named
public class Posts {
    private List<Post> posts = new ArrayList<>();
    private Post currentPost = new Post();
    
    public Posts(){
        currentPost = new Post(-1,-1,"", null,"");
        getPostsFromDB();
    }
    
    public List<Post> getPosts(){
        return posts;
    }
    public Post getCurrentPost(){
        return currentPost;
    }
    public void setCurrentPost(Post currentPost){
        this.currentPost = currentPost;
    }
    public String viewPost(Post post) {
        currentPost = post;
        return "viewPost";
    }
    
    public String addPost() {
        currentPost = new Post(-1, -1, "", null, "");
        return "editPost";
    }
    
    public String editPost(){
        return "editPost";
    }
    
    public String cancelPost(){
        int id = currentPost.getId();
        getPostsFromDB();
        currentPost = getPostById(id);
        return "viewPost";
    }
    
    public String savePost(User user) {
        try (Connection conn = DBUtils.getConnection()) {
            // If there's a current post, update rather than insert
            if (currentPost.getId() >= 0) {
                String sql = "UPDATE posts SET title = ?, contents = ? where id = ?";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, currentPost.getTitle());
                pstmt.setString(2, currentPost.getContents());
                pstmt.setInt(3, currentPost.getId());
                System.out.println("Update: " + pstmt.executeUpdate());
            } else {
                String sql = "INSERT INTO posts (user_id, title, created_time, contents) VALUES (?,?,NOW(),?)";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setInt(1, user.getId());
                pstmt.setString(2, currentPost.getTitle());
                pstmt.setString(3, currentPost.getContents());
                System.out.println("Insert: " + pstmt.executeUpdate());
            }
        } catch (SQLException ex) {
            Logger.getLogger(Posts.class.getName()).log(Level.SEVERE, null, ex);
        }
        getPostsFromDB();
        // Update the currentPost so that its details appear after navigation
        currentPost = getPostByTitle(currentPost.getTitle());
        return "viewPost";
    }
    
    private void getPostsFromDB(){
        try {
            Connection conn = DBUtils.getConnection();
            
            posts = new ArrayList<>();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM posts");
            while (rs.next()){
                Post p = new Post(rs.getInt("id"),
                                  rs.getInt("user_id"),
                                  rs.getString("title"),
                                  rs.getDate("created_time"),
                                  rs.getString("contents"));
                posts.add(p);
            }
                    
        } catch (SQLException ex) {
            Logger.getLogger(Posts.class.getName()).log(Level.SEVERE, null, ex);
            posts = new ArrayList<>();
        }
    }
    
    public Post getPostById(int id){
        for (Post p : posts) {
            if (p.getId() == id) {
                return p;
            }
        }
        return null;
    }
    
    public Post getPostByTitle(String title) {
        for (Post p : posts) {
            if (p.getTitle().equals(title)) {
                return p;
            }
        }
        return null;
    }
}
