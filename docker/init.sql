--
-- PostgreSQL database dump
--

SET statement_timeout = 0;
SET lock_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;

SET default_tablespace = '';

SET default_with_oids = false;

---
--- drop tables
---

DROP TABLE IF EXISTS users;

---
--- create tables
--

-- id — уникальный идентификатор, автоматически увеличивается.

-- name — имя пользователя, обязательно к заполнению.

-- email — адрес почты, обязательно и уникален.

-- age — возраст с проверкой диапазона.

-- created_at — дата и время создания записи, по умолчанию ставится текущее время.


CREATE TABLE users (
                       id SERIAL PRIMARY KEY,
                       name VARCHAR(100) NOT NULL,
                       email VARCHAR(255) NOT NULL UNIQUE,
                       age INT CHECK (age >= 0 AND age <= 100),
                       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);