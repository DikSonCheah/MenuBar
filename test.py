import os 
from github import Github

g = Github(os.environ.get('token'))
for repo in g.get_user().get_repos():
    print(repo.name)
