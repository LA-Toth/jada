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
or SSH (see [JSSH](https://github.com/LA-Toth/jsocks))


Setup
-----

The code requires Java records fully supported in Java 16.
It works with older Java versions if the pom.xml is updated
as needed.

The proxies has to be defined in a YAML file, an example:
[conf.example.yaml](conf.example.yaml). As of now it's quite fragile,
as the YAML loader is a naive implementation providing minimal
functionality.

Plans (TODOs)
-------------

* Improve YAML loader code
* Separate package (Jar) to generate a config file either from console
  or interactively.
* Migrate and improve the SSH code from
  [JSSH](https://github.com/LA-Toth/jsocks)
* Add minimalistic rules - disallow connection to the local machine
  if it is not explicitly allowed.
