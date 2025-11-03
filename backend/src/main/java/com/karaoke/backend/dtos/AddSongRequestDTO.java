package com.karaoke.backend.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor; 

@Data
@NoArgsConstructor
@AllArgsConstructor 
public class AddSongRequestDTO {
    private String songTitle; 
    
    private String userId;
    private String userName;
}