package addingComm;

import java.util.ArrayList;

import battlecode.common.*;

import static battlecode.common.Direction.*;

// Singly linked list
class SLinkedList<T>
{
	class Node<T>
	{
		private T value;
		private Node next,last;
		public Node(T val,Node nxt)
		{
			value=val;
			next=nxt;
		}
	}

	public class Iterator
	{
		private Node it;
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

	Node head,last;

	public SLinkedList()
	{
		head=last=new Node(null,null);	// head node
	}

	public void add(T val)
	{
		last.next=new Node(val,null);
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
	class Node<T>
	{
		private T value;
		private Node left,right;
		public Node(T val,Node left,Node right)
		{
			value=val;
			this.left=left;
			this.right=right;
		}
	}

	public class Iterator
	{
		private Node it;
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

	Node head,last;

	public DLinkedList()
	{
		head=new Node(null,head,head);	// head node
		last=head;
	}

	public boolean isEmpty()
	{
		return head.right==null;
	}
	public void add(T val)
	{
		last=new Node(val,last,null);
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
			Node tmp=last;
			last=last.left;
			last.right=null;
			return (T) tmp.value;
		}
	}

	public boolean findNremove(T target)
	{
		if(isEmpty())
		{
			return false;
		}
		else
		{
			Node now=head.right;
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
	public <T> boolean isNew(T vl)
    {
        Iterator it=getIterator();
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