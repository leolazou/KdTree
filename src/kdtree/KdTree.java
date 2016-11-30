package kdtree;

import java.util.ArrayList;
import java.util.Collections;
import java.lang.Math;

public class KdTree<Point extends PointI>
{
	/** A node in the KdTree
	 */
	public class KdNode 
	{
		KdNode child_left_, child_right_;
		Point pos_;
		int d_; 	/// dimension in which the cut occurs
		
		KdNode(Point p, int d){
			this.pos_ = p;
			this.d_ = d;
			this.child_left_ = null;
			this.child_right_ = null;
		}

		KdNode(Point p, int d, KdNode l_child, KdNode r_child){
			this.pos_ = p;
			this.d_ = d;
			this.child_left_ = l_child;
			this.child_right_ = r_child;
		}
		
		/** 
		 * if strictly negative the query point is in the left tree
		 * TODO: equality is problematic if we want a truly balanced tree
		 */
		int dist1D(Point p) { 
			return p.get(d_) - pos_.get(d_);
		}
	}
	
	/////////////////
    /// Attributs ///
    /////////////////

	private final int dim_; /// dimension of space
	private int n_points_; /// number of points in the KdTree
	
	private KdNode root_; /// root node of the KdTree

    //////////////////
    /// Constructor///
    //////////////////

	/** Initialize an empty kd-tree
	 */
	KdTree(int dim) {
		this.dim_ = dim;
		this.root_ = null;
		this.n_points_ = 0;
	}

	/** Initialize the kd-tree from the input point set
	 *  The input dimension should match the one of the points
	 */
	KdTree(int dim, ArrayList<Point> points, int max_depth, int dimension) {
		this.dim_ = dim;
		this.n_points_ = points.size();
		build(dim, points, max_depth, dimension);
	}	
		//TODO: replace by a balanced initialization
	private KdTree build(int dim, ArrayList<Point> points, int max_depth,int dimension){
		if (!points.isEmpty()){
			if (max_depth!=0){
				ArrayList<Integer> tri_point = new ArrayList<Integer>();
				for(int i = 0; i< points.size(); i++) {
					tri_point.add(i, (points.get(i)).get(dim));
				}
				ArrayList<Integer> memoire_point = tri_point;
				//memoire_point va servir à garder en mémoire les points sous la forme de leur coordonnée selon dim
				Collections.sort(tri_point);
				int mediane = tri_point.get(((tri_point.size())/2)+1);
				
				// Maintenant que l'on a trouvé le point median, on l'insère en premier dans l'arbre
				this.insert(points.get(memoire_point.indexOf(mediane)));
				ArrayList<Point> points_petit = points;
				ArrayList<Point> points_grand = points;
				for (int i = ((tri_point.size())/2); i>-1; i--){
					points_grand.remove(memoire_point.indexOf(tri_point.get(i)));
				}
				for (int i = ((tri_point.size())/2)+2; i<tri_point.size(); i++){
					points_petit.get(memoire_point.indexOf(tri_point.get(i)));
				}
			
				/* On parcourt maintenant tri_point dans les 2 sens pour insérer les points suivants dans l'arbre
				en prenant à chaque fois la médiane
				On s'assure également de changer de dimension à chaque fois afin d'utiliser une meilleure
				heuristique pour la dimension de coupe*/
				this.build((dim+1)%dimension, points_petit , max_depth-1, dimension);
				this.build((dim+1)%dimension, points_grand , max_depth-1, dimension);
			}
			// On prend maintenant en compte la profondeur maximale de l'arbre
			else {
				ArrayList<Integer> tri_point = new ArrayList<Integer>();
				int bary[] = {};
				for (int j = 1; j<dimension+1; j++){
					for(int i = 0; i< points.size(); i++) {
						tri_point.add(i, (points.get(i)).get(j));
						bary[j]+= tri_point.get(i);
					}
					bary[j]=bary[j]/tri_point.size();
				}
				if (dimension==2){
					Point g = (Point) new Point2i (bary[0], bary[1]);
					this.insert(g);
				}
				else {
					Point g = (Point) new Point3i (bary[0], bary[1], bary[2]);
					this.insert(g);
				}
			}
			
		}
		return (this);
	}
		/*for (int i = ((tri_point.size())/2); i>-1; i--){
			this.insert(points.get(memoire_point.indexOf(tri_point.get(i))));
		}
		for (int i = ((tri_point.size())/2)+2; i<tri_point.size(); i++){
			this.insert(points.get(memoire_point.indexOf(tri_point.get(i))));
		}*/
	
	
	  
	/////////////////
	/// Accessors ///
	/////////////////

	int dimension() { return dim_; }

	int nb_points() { return n_points_; }

	void getPointsFromLeaf(ArrayList<Point> points) {
		getPointsFromLeaf(root_, points);
	}

	 
	///////////////
	/// Mutator ///
	///////////////

	/** Insert a new point in the KdTree.
	 */
	void insert(Point p) {
		n_points_ += 1;
		
		if(root_==null) 
			root_ = new KdNode(p, 0);
		
		KdNode node = getParent(p);
		if(node.dist1D(p)<0) {
			assert(node.child_left_==null);
			node.child_left_ = new KdNode(p, (node.d_+1)%dim_);
		} else {
			assert(node.child_right_==null);
			node.child_right_ = new KdNode(p, (node.d_+1)%dim_);
		}
	}
	void delete(Point p) {
		assert(false);
	}

	///////////////////////
	/// Query Functions ///
	///////////////////////

	/** Return the node that would be the parent of p if it has to be inserted in the tree
	 */
	KdNode getParent(Point p) {
		assert(p!=null);
		
		KdNode next = root_, node = null;

		while (next != null) {
			node = next;
			if ( node.dist1D(p) < 0 ){
				next = node.child_left_;
			} else {
				next = node.child_right_;
			}
		}
		
		return node;
	}
	
	/** Check if p is a point registered in the tree
	 */
	boolean contains(Point p) {
        return contains(root_, p);
	}

	/** Get the nearest neighbor of point p
	 */
    public Point getNN(Point p)
    {
    	assert(root_!=null);
        return getNN(root_, p, root_.pos_);
    }

	///////////////////////
	/// Helper Function ///
	///////////////////////

    /** Add the points in the leaf nodes of the subrre defined by root 'node'
     * to the array 'point'
     */
	private void getPointsFromLeaf(KdNode node, ArrayList<Point> points)
	{
		if(node.child_left_==null && node.child_right_==null) {
			points.add(node.pos_);
		} else {
		    if(node.child_left_!=null)
		    	getPointsFromLeaf(node.child_left_, points);
		    if(node.child_right_!=null)
		    	getPointsFromLeaf(node.child_right_, points);
		}
	 }
	
	/** Search for a better solution than the candidate in the subtree with root 'node'
	 *  if no better solution is found, return candidate
	 */
	 private Point getNN(KdNode node, Point point, Point candidate)
	 {
	    if ( point.sqrDist(node.pos_) <  point.sqrDist(candidate)) 
	    	candidate = node.pos_;

	    int dist_1D = node.dist1D(point);
	    KdNode n1, n2;
	    if( dist_1D < 0 ) {
	    	n1 = node.child_left_;
	    	n2 = node.child_right_;
	    } else {
	    	// start by the right node
	    	n1 = node.child_right_;
	    	n2 = node.child_left_;
	    }

	    if(n1!=null)
	    	candidate = getNN(n1, point, candidate);

	    if(n2!=null && dist_1D*dist_1D < point.sqrDist(candidate)) 
	    	candidate = getNN(n2, point, candidate);
		 
		 return candidate;
	 }
	 
	private boolean contains(KdNode node, Point p) {
        if (node == null) return false;
        if (p.equals(node.pos_)) return true;

        //TODO : assume the "property" is strictly verified
        if (node.dist1D(p)<0)
            return contains(node.child_left_, p);
        else
            return contains(node.child_right_, p);
	}
	
}


