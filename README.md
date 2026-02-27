Jada - a set of proxies
-----------------------

*Jada* means: knowing. It reflects that it is written in Java -
matching the first letter -, and may know the data sent
and received in the connection.

Origin
------

The implemented proxies are from my need.

As I developed proxies in C (see [Zorp GPL](https://github.com/balasys/zorp),
[LibZorpLL](https://github.com/balasys/libzorpll), etc.),
I became familiar with a specific log format which I can read with ease,
so I kept it with a few trivial others, namely:
  * logger names are like Zorp proxy names ("something:1", "something:2", etc.)
  * hexadecimal dump of a binary data (data line 0xabcd: ...)
  * log line: Starting proxy instance
  * log line: Ending proxy instance
  * has an explicit config file (not Python, but YAML)

The protocol details are publicly available, like SOCKS,
or SSH.


Setup
-----

The initial code required only records (Java 16). Then the pom.xml
is updated to use Java 25, as of now (early 2026) the code may work
with older Java versions.

The proxies have to be defined in a YAML file, an example:
[conf.example.yaml](conf.example.yaml). As of now it's quite fragile,
as the YAML loader is a naive implementation providing minimal
functionality.

Terminology
-----------

As the connection looks like `client - proxy - server`,
there are two TCP connections: client-side and server-side.
The proxy acts as a server for the client, so client-side and server
are interchangeable, similarly the server-side and client. This is confusing, though.
Example: On the client-side the DH Group implementation is DHGServer.

The TCP connections have two directions, identified as modes, `MODE_IN`
and `MODE_OUT`, from the proxy's point of view.


Supported proxies
-----------------

* plug: just drops the data to the other side as-is
* socks: a SOCKSv5 proxy without authentication
* SSH: in progress to reach milestone 1
  * SSH milestone 1: `ssh-rsa` server keys,
    inband destination selection from user name,
    like `remote-user@server-name`
    so client could be: `ssh remote-user@server-name@jada-proxy-address`
    and `password` authentication method
    every data is copied from one side to another (except the modified username)
  * SSH milestone 2: keyboard-interactive and public key authentications
    (requires one-side commuinication with the SSH agent)
  * SSH milestone 3: policies to decied what's allowed:
    * can the user connect to that server?
    * can the user open a specific channel? (note that even if the answer is NO
      for the agent forwarding, that may be needed on the client side only)
