package entity;

import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "users")
public class Player {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id")
    private int id;

    @Basic
    @Column(name = "name")
    private String name;

    /*@OneToMany(mappedBy = "player1")
    private List<Games> gamesAsPlayer1;

    @OneToMany(mappedBy = "player2")
    private List<Games> gamesAsPlayer2;*/
    Player() {
    }

    public Player(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
