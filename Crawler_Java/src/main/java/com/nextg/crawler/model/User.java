package com.nextg.crawler.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
    @Entity(name = "users")
    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public class User {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;
        private String username;
        private String email;
        private String password;
        private String user_role;
        private boolean emailVerifired;
        private String userImage;
    }


    
    