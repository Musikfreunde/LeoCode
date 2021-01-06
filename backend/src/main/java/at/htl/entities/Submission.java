package at.htl.entities;

import io.quarkus.hibernate.orm.panache.PanacheEntity;

import javax.persistence.*;

import at.htl.entities.LeocodeStatus;

@Entity
public class Submission extends PanacheEntity {

    public String pathToZip;
    @Enumerated(value = EnumType.STRING)
    public LeocodeStatus status;
    public String author;
    public String result;
    @OneToOne
    public Example example;

    public Submission() {
        this.status = LeocodeStatus.CREATED;
    }

    public Submission(String pathToZip, LeocodeStatus status, String author) {
        this.pathToZip = pathToZip;
        this.status = status;
        this.author = author;
    }

    @Override
    public String toString() {
        return "Submition{" +
                "pathToZip='" + pathToZip + '\'' +
                ", status=" + status +
                ", author='" + author + '\'' +
                ", id=" + id +
                '}';
    }
}
