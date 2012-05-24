def test_generic_user_class():
    class User1(object):
        def __init__(self, x):
            """
            :type x: T
            :rtype: User1 of T
            """
            self.x = x

        def get(self):
            """
            :rtype: T
            """
            return self.x

        def put(self, value):
            """
            :type value: T
            """
            self.x = value

    c = User1(10)
    print(c.get() + <warning descr="Expected type 'one of (int, long, float, complex)', got 'str' instead">'foo'</warning>)
    c.put(14)
    c.put(<weak_warning descr="Expected type 'int' (matched generic type 'T'), got 'str' instead">'foo'</weak_warning>)


