
from math import sqrt

max_rsq = 65 # this is 1 square outside of the HQ vision range
for rsq in range(max_rsq+1):
    x = 0
    while x*x <= rsq:
        diff = rsq - x*x
        # force |y| >= |x|
        if diff >= x*x:
            y = int(sqrt(diff))
            if y*y == diff:
                print('{{{0}, {1}}}, '.format(x, y), end='')
                if y > 0 or x > 0:
                    print('{{-{1}, {0}}}, '.format(x, y), end='')
                    print('{{-{0}, -{1}}}, '.format(x, y), end='')
                    print('{{{1}, -{0}}}, '.format(x, y), end='')
        x += 1


