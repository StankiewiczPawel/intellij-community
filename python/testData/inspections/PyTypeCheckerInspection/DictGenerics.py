def test_dict_generics(d):
    """
    :type d: dict from int to unicode
    """
    xs = d.items()
    d2 = dict(xs)
    for k, v in d2.items():
        print k + <warning descr="Expected type 'one of (int, long, float, complex)', got 'unicode' instead">v</warning>
