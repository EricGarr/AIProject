package test;

public class Main {

	public static void main(String[] args) {
		Node[][] nodes = new Node[18][16];

		//make nodes for entire map
		int nodeNum = 0;
		for(int width = 0; width < 18; width++){//make nodes for all specified locations
			for(int height = 0; height < 16; height++){
				int x = width*100;
				int y = height*60;
				
/*here is where we could check if the nodes have objects in their spaces 
 * so we don't add them to the end list.*/
				
				nodes[width][height] = new Node(x, y);//create node and add it to list
				nodeNum++;
			}
			System.out.println(nodeNum);
		}
		
		//list of nodes is done, so check edges per node.
		for(int i = 0; i < 18; i++) {
			for(int j = 0; j < 16; j++) {
				nodes[i][j].getEdges(nodes);
			}
		}
	}
	
}

