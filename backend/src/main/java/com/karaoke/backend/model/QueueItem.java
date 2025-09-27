/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.karaoke.backend.model;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Data;

@Data
public class QueueItem {

    private final String queueItemId;
    private User user;
    private Song song;
    private final LocalDateTime timestampAdded;

    public QueueItem(User user, Song song) {
        this.queueItemId = UUID.randomUUID().toString();
        this.timestampAdded = LocalDateTime.now();
        this.user = user;
        this.song = song;
    }
}
