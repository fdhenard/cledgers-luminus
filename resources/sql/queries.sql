-- :name create-user! :! :n
-- :doc creates a new user record
INSERT INTO users
(username, first_name, last_name, email, pass, admin, is_active)
VALUES (:username, :first_name, :last_name, :email, :pass, :admin, :is_active)

-- :name update-user! :! :n
-- :doc update an existing user record
UPDATE users
SET first_name = :first_name, last_name = :last_name, email = :email
WHERE id = :id

-- :name get-user :? :1
-- :doc retrieve a user given the id.
SELECT * FROM users
WHERE id = :id

-- :name get-user-by-uname :? :1
SELECT * FROM users
WHERE username = :username

-- :name delete-user! :! :n
-- :doc delete a user given the id
DELETE FROM users
WHERE id = :id
