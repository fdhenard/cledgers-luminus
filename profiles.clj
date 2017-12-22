;; WARNING
;; The profiles.clj file is used for local environment variables, such as database credentials.
;; This file is listed in .gitignore and will be excluded from version control by Git.

{:profiles/dev  {:env {:database-url "postgresql://localhost/cledgers_luminus_dev?user=cledgers-luminus&password=cledgers-luminus"}}
 :profiles/test {:env {:database-url "postgresql://localhost/luminus_test_repo_test?user=db_user_name_here&password=db_user_password_here"}}}
