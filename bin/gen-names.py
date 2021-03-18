#!/usr/bin/env python3
#  Copyright 2020-2021 Laszlo Attila Toth
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.

import os

import typing

SRC = 'sshnames.txt'
TARGET = 'src/main/java/me/laszloattilatoth/jada/proxy/ssh/core/Name.java'

ROOT_DIR = os.path.join(os.path.dirname(os.path.abspath(__file__)), '..')
INDENT = ' ' * 4
UNKNOWN = '(unknown)'


def get_names(filename: str) -> typing.List[str]:
    names = [UNKNOWN]

    with open(filename) as f:
        for line in f:
            line = line.rstrip()
            if not line or line.startswith('#') or line.startswith(' '):
                continue

            name = line.split(' ')[0].strip('"')
            if name not in names:
                names.append(name)

    return names


def _(name: str):
    return name.replace('(', '').replace(')', '').replace('-', '_').upper()


def write_numeric_constants(f, names: typing.List[str]):
    idx = 0
    for name in names:
        f.write(f"{INDENT}public static final int SSH_NAME_{_(name)} = {idx};\n")
        idx += 1

    f.write(f"{INDENT}public static final int SSH_NAME_MAX = {idx - 1};\n")


def write_arrays(f):
    f.write(f"{INDENT}private static final Map<String, Integer> SSH_NAME_STRING_TO_ID = new HashMap<>();\n")
    f.write(f"{INDENT}private static final String[] SSH_NAMES = new String[SSH_NAME_MAX + 1];\n")


def write_static_init(f, names: typing.List[str]):
    f.write(f'{INDENT}static {{\n')

    idx = 0
    for name in names:
        f.write(f"{INDENT}{INDENT}SSH_NAME_STRING_TO_ID.put(\"{name}\", {idx});\n")
        idx += 1
    f.write("\n")

    idx = 0
    for name in names:
        f.write(f"{INDENT}{INDENT}SSH_NAMES[{idx}] = \"{name}\";\n")
        idx += 1

    f.write(f'{INDENT}}}\n')


def write_header(f):
    f.write("\n".join([
        '/// This is a generated file, do not modify',
        '/// @see bin/gen-names.py in the source code',
        '',
        'package me.laszloattilatoth.jada.proxy.ssh.core;',
        '',
        'import java.util.HashMap;',
        'import java.util.Map;',
        '',
        'public class Name {',
        f'{INDENT}public static final String STR_UNKNOWN = "{UNKNOWN}";'
    ]) + "\n")


def write_methods_and_footer(f):
    f.write("\n".join([
        f'{INDENT}public static String getName(int nameId) {{',
        f'{INDENT}{INDENT}if (nameId < 0 || nameId > SSH_NAME_MAX)',
        f'{INDENT}{INDENT}{INDENT}return STR_UNKNOWN;',
        f'{INDENT}{INDENT}else',
        f'{INDENT}{INDENT}{INDENT}return SSH_NAMES[nameId];',
        f'{INDENT}}}',
        '',
        f'{INDENT}public static int getNameId(String name) {{',
        f'{INDENT}{INDENT}return SSH_NAME_STRING_TO_ID.getOrDefault(name, 0);',
        f'{INDENT}}}',
        '',
        f'{INDENT}public static boolean hasName(String name) {{',
        f'{INDENT}{INDENT}return SSH_NAME_STRING_TO_ID.getOrDefault(name, -1) > -1;',
        f'{INDENT}}}',
        '',
        f'{INDENT}public static boolean isUnknownName(String name) {{',
        f'{INDENT}{INDENT}return !hasName(name);',
        f'{INDENT}}}',
        f'}}',
    ]) + "\n")


def create_target(filename: str, names: typing.List[str]):
    with open(filename, 'wt') as f:
        write_header(f)
        f.write("\n")
        write_numeric_constants(f, names)
        f.write("\n")
        write_arrays(f)
        f.write("\n")
        write_static_init(f, names)
        f.write("\n")
        write_methods_and_footer(f)


def main(src: str, dst: str):
    create_target(dst, get_names(src))


if __name__ == '__main__':
    main(
        os.path.abspath(os.path.join(ROOT_DIR, SRC)),
        os.path.abspath(os.path.join(ROOT_DIR, TARGET)),
    )
