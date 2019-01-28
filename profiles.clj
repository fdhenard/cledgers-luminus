;; WARNING
;; The profiles.clj file is used for local environment variables, such as database credentials.
;; This file is listed in .gitignore and will be excluded from version control by Git.

;; F. Henard 12/22/2017 - added to version control because it was lost afer deleting repo, and
;; was difficult to figure out what to do

{
 ;; :profiles/dev  {:env {:database-url "postgresql://localhost/cledgers_luminus_dev?user=cledgers_luminus&password=cledgers-luminus"}}
 :profiles/dev  {:env {:database-url "postgresql://localhost/cledgers_luminus?user=cledgers_luminus"}}
  :profiles/test {:env {:database-url "postgresql://localhost/luminus_test_repo_test?user=db_user_name_here&password=db_user_password_here"}}}
