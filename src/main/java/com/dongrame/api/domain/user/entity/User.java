package com.dongrame.api.domain.user.entity;

import com.dongrame.api.domain.bookmark.entity.Bookmark;
import com.dongrame.api.domain.review.entity.CommentLike;
import com.dongrame.api.domain.review.entity.Review;
import com.dongrame.api.domain.review.entity.ReviewComment;
import com.dongrame.api.domain.review.entity.ReviewLike;
import com.dongrame.api.domain.user.dto.UserUpdateRequestDto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String kakaoId;

    @Column(nullable = false)
    private String nickname;

    @Column(nullable = false)
    private String profileImage;

    @Column(unique = true)
    private String email;

    private int age;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    private UserType userType;

    @Column(nullable = false)
    private boolean active = true;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Review> reviews = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<ReviewComment> reviewComments = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<ReviewLike> reviewLikes = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<CommentLike> commentLikes = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Bookmark> bookmarks = new ArrayList<>();

    public void updateUserType(UserType userType) {
        this.userType = userType;
    }

    public void update(UserUpdateRequestDto dto) {
        this.nickname = dto.getUsername();
        this.email = dto.getEmail();
        this.userType = dto.getUserType();
        this.age = dto.getAge();
        this.gender = dto.getGender();
    }
}
