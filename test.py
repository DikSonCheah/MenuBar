import argparse
from github import Github


def arguments():
    parser = argparse.ArgumentParser()
    parser.add_argument("token", help="Github authentication token")
    args = parser.parse_args()
    return args


def create_issue(token):
    g = Github()
    for repo in g.get_user().get_repos():
        print(repo.name)


def main():
    args = arguments()
    create_issue(args.token)


if __name__ == "__main__":
    main()