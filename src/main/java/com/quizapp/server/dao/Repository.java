package com.quizapp.server.dao;

import com.quizapp.exception.QuizAppException;
import java.util.List;
import java.util.Optional;

/**
 * Generic interface for the basic database operations (Create, Retrieve, Update, Delete).
 * Each DAO implements it for one type: Repository<User>, Repository<Quiz>, ...
 *
 * @param <T> the type of object stored
 */
public interface Repository<T> {

    /** Inserts the object and returns its new id. */
    int add(T item) throws QuizAppException;

    Optional<T> findById(int id) throws QuizAppException;

    List<T> findAll() throws QuizAppException;

    void update(T item) throws QuizAppException;

    void delete(int id) throws QuizAppException;
}
