/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author geza
 */
public class Extrema {

	public void initval(final int x, final int y) {
		ltx = lbx = rtx = rbx = 
		tx = bx = lx = rx =
		x;

		lty = lby = rty = rby =
		ty = by = ly = ry =
		y;
	}

	public void update(final int x, final int y) {
		if (x+y > rbx+rby) {
			rbx = x;
			rby = y;
		} if (x+y < ltx+lty) { //(-x-y > -ltx-lty)
			ltx = x;
			lty = y;
		} if (-x+y > -lbx+lby) {
			lbx = x;
			lby = y;
		} if (x-y > rtx-rty) {
			rtx = x;
			rty = y;
		}
		// cardinal
		if (y > by) {
			bx = x;
			by = y;
		} if (y < ty) {
			tx = x;
			ty = y;
		} if (x < lx) {
			lx = x;
			ly = y;
		} if (x > rx) {
			rx = x;
			ry = y;
		}
	}

	// diagonal
	public int ltx = 0;
	public int lty = 0;
	public int lbx = 0;
	public int lby = 0;
	public int rtx = 0;
	public int rty = 0;
	public int rbx = 0;
	public int rby = 0;
	// cardinal
	public int tx = 0;
	public int ty = 0;
	public int bx = 0;
	public int by = 0;
	public int lx = 0;
	public int ly = 0;
	public int rx = 0;
	public int ry = 0;
}
