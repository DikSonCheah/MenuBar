import argparse
import os
from github import Github



def create_issue(token):
    g = Github()
    for repo in g.get_user().get_repos():
        print(repo.name)


def main():
    print(os.environ.get("token"))
    create_issue(os.environ.get("token"))


if __name__ == "__main__":
    main()

