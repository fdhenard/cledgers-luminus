-- :name create-user! :! :n
-- :doc creates a new user record
INSERT INTO cledgers_user
(username, first_name, last_name, email, pass, is_admin, is_active)
VALUES (:username, :first_name, :last_name, :email, :pass, :is_admin, :is_active)

-- :name update-user! :! :n
-- :doc update an existing user record
UPDATE cledgers_user
SET first_name = :first_name, last_name = :last_name, email = :email
WHERE id = :id

-- :name get-user :? :1
-- :doc retrieve a user given the id.
SELECT * FROM cledgers_user
WHERE id = :id

-- :name get-user-by-uname :? :1
SELECT * FROM cledgers_user
WHERE username = :username

-- :name delete-user! :! :n
-- :doc delete a user given the id
DELETE FROM cledgers_user
WHERE id = :id

-- :name create-xaction! :! :n
-- :doc creates a new xaction
INSERT INTO xaction
(description, amount, date, created_by_id, uuid, payee_id)
VALUES (:description, :amount, :date, :created-by-id, :uuid, :payee-id)

-- :name create-xaction-and-payee! :! :n
-- :doc creates a new payee and new xaction
WITH payee_insert_id_row AS (
  INSERT INTO payee
    (name, created_by_id)
  VALUES
    (:payee-name, :created-by-id)
  RETURNING id
)
INSERT INTO xaction
  (uuid, description, payee_id, amount, date, created_by_id)
VALUES
  (:uuid,
   :description,
   (SELECT id FROM payee_insert_id_row),
   :amount,
   :date,
   :created-by-id)
