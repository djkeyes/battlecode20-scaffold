
# zips a package, and sanitizes the filename to avoid leaking strategies

import sys
import os
import shutil


def parse_input():
    if len(sys.argv) != 3:
        print('usage: python zip.py package_name sanitized_name')
        exit(1)
    package = sys.argv[1]
    # TODO: it might be convenient to just insert the commit hash + hashed package name here
    sanitized_package = sys.argv[2]
    return package, sanitized_package

def sanitize_files(directory, old, new):
    for dname, dirs, files in os.walk(directory):
        for fname in files:
            fpath = os.path.join(dname, fname)
            with open(fpath) as f:
                s = f.read()
            s = s.replace(old, new)
            with open(fpath, "w") as f:
                f.write(s)

def main():
    package, sanitized_package = parse_input()
    root_dir = os.path.dirname(os.path.dirname(os.path.realpath(__file__)))
    src_dir = os.path.join(root_dir, 'src')
    package_dir = os.path.join(src_dir, package)
    tmp_dir = './tmp/'
    sanitized_package_dir = os.path.join(tmp_dir, sanitized_package)

    if os.path.exists(tmp_dir):
        shutil.rmtree(tmp_dir)
    os.makedirs(tmp_dir)
    shutil.copytree(package_dir, sanitized_package_dir)
    sanitize_files(sanitized_package_dir, package, sanitized_package)
    shutil.make_archive(sanitized_package, 'zip', tmp_dir)
    shutil.rmtree(tmp_dir)

    print('file has been saved to {}.zip'.format(sanitized_package))

if __name__=="__main__":
    main()
