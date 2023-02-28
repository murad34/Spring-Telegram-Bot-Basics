package com.example.springdemobot.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity(name = "ads")
public class Ads {

    @Id
    private Long id;

    private String reklama;

}
