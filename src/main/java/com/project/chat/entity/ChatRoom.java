package com.project.chat.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ChatRoom {
    @Id
    @Column
    private String id;

    @Column
    private String name;

    @Column
    private int participantCnt;
}
