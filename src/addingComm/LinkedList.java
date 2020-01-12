package addingComm;

import java.util.ArrayList;

import battlecode.common.*;

import static battlecode.common.Direction.*;

// Singly linked list
class SLinkedList<T>
{
	static class Node<T>
	{
		private final T value;
		private Node<T> next,last;
		public Node(final T val, final Node<T> nxt)
		{
			value=val;
			next=nxt;
		}
	}

	public class Iterator
	{
		private Node<T> it;
		public Iterator()
		{
			it=head;
		}
		public boolean hasNext()
		{
			return it.next!=null;
		}
		public T getNext()
		{
			if(hasNext())
			{
				it=it.next;
				return (T) it.value;
			} 
			else
			{
				return null;
			}
		}
	}

	public Iterator getIterator()
	{
		return new Iterator();
	}

	Node<T> head,last;

	public SLinkedList()
	{
		head=last= new Node<>(null, null);	// head node
	}

	public void add(final T val)
	{
		last.next= new Node<>(val, null);
		last=last.next;
	}

	public boolean isEmpty()
	{
		return head.next==null;
	}

}

//Doubly Linked list
class DLinkedList<T>
{
	static class Node<T>
	{
		private final T value;
		private Node<T> left,right;
		public Node(final T val, final Node<T> left, final Node<T> right)
		{
			value=val;
			this.left=left;
			this.right=right;
		}
	}

	public class Iterator
	{
		private Node<T> it;
		public Iterator()
		{
			it=head;
		}
		public boolean hasNext()
		{
			return it.right!=null;
		}
		public T getNext()
		{
			if(hasNext())
			{
				it=it.right;
				return it.value;
			} 
			else
			{
				return null;
			}
		}
	}

	public Iterator getIterator()
	{
		return new Iterator();
	}

	Node<T> head,last;

	public DLinkedList()
	{
		head= new Node<>(null, null, null);	// head node
		last=head;
	}

	public boolean isEmpty()
	{
		return head.right==null;
	}
	public void add(final T val)
	{
		last= new Node<>(val, last, null);
		last.left.right=last;
	}

	public T remove()
	{
		if(isEmpty())
		{
			return null;
		}
		else
		{
			final Node<T> tmp=last;
			last=last.left;
			last.right=null;
			return (T) tmp.value;
		}
	}

	public boolean findNremove(final T target)
	{
		if(isEmpty())
		{
			return false;
		}
		else
		{
			Node<T> now=head.right;
			do
			{
				if(now.value.equals(target))
				{
					now.left=now.right;
					if(now.right!=null)
					{
						now.right.left=now.left;
					}
					return true;
				}
				now=now.right;
			}while(now!=null);
			return false;
		}
	}

	// Check if value vl is already contained by an element in the list?
	public boolean isNew(final T vl)
    {
        final Iterator it=getIterator();
        while(it.hasNext())
        {
            if(vl.equals(it.getNext()))
            {
                return false;
            }
        }
        return true;
    }

}