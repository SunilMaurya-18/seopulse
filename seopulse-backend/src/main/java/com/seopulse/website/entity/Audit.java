package com.seopulse.website.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "audit")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Audit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 2048)
    private String url;
    @Column(nullable = false)
    private Integer score;

}
