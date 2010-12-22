/*
 * This file is part of Geomajas, a component framework for building
 * rich Internet applications (RIA) with sophisticated capabilities for the
 * display, analysis and management of geographic information.
 * It is a building block that allows developers to add maps
 * and other geographic data capabilities to their web applications.
 *
 * Copyright 2008-2010 Geosparc, http://www.geosparc.com, Belgium
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.geomajas.puregwt.client.spatial;

import org.geomajas.geometry.Coordinate;

/**
 * 2-dimensional vector. We're trying to keep the mathematical difference between points and vectors alive.
 *
 * @author Pieter De Graef
 */
public class Vector2D {

	private double x;

	private double y;

	// -------------------------------------------------------------------------
	// Constructors:
	// -------------------------------------------------------------------------

	public Vector2D() {
		x = 0;
		y = 0;
	}

	public Vector2D(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public Vector2D(Coordinate c) {
		this.x = c.getX();
		this.y = c.getY();
	}

	/**
	 * Takes the difference between 2 coordinates to calculate the vector. (Vector = Coordinate2 - Coordinate1)
	 *
	 * @param c1
	 *            First coordinate
	 * @param c2
	 *            Second coordinate
	 */
	public Vector2D(Coordinate c1, Coordinate c2) {
		this.x = c1.getX() - c2.getX();
		this.y = c1.getY() - c2.getY();
	}

	// -------------------------------------------------------------------------
	// Class specific functions:
	// -------------------------------------------------------------------------

	/**
	 * Add another vector to this one.
	 *
	 * @param vector2d
	 *            The other vector.
	 */
	public void add(Vector2D vector2d) {
		x += vector2d.x;
		y += vector2d.y;
	}

	/**
	 * Subtract another vector from this one.
	 *
	 * @param vector2d
	 *            The other vector.
	 */
	public void subtract(Vector2D vector2d) {
		x -= vector2d.x;
		y -= vector2d.y;
	}

	/**
	 * Scale this vector.
	 *
	 * @param xFactor
	 *            Scale the X-factor with this value.
	 * @param yFactor
	 *            Scale the Y-factor with this value.
	 */
	public void scale(double xFactor, double yFactor) {
		this.x *= xFactor;
		this.y *= yFactor;
	}

	/**
	 * Translate this vector.
	 *
	 * @param xDist
	 *            Translate the X-factor with x.
	 * @param yDist
	 *            Translate the Y-factor with y.
	 */
	public void translate(double xDist, double yDist) {
		this.x += xDist;
		this.y += yDist;
	}

	/**
	 * Calculate the distance between 2 vector by using Pythagoras' formula.
	 *
	 * @param vector2d
	 *            The other vector.
	 * @returns The distance between these 2 vectors as a Double.
	 */
	public double distance(Vector2D vector2d) {
		double a = vector2d.x - x;
		double b = vector2d.y - y;
		return Math.sqrt(a * a + b * b);
	}

	/**
	 * Normalize this vector.
	 */
	public void normalize() {
		double len = this.length();
		if (len == 0) {
			return;
		}
		x /= len;
		y /= len;
	}

	/**
	 * Return the length of this vector. (Euclides)
	 *
	 * @returns The length as a Double.
	 */
	public double length() {
		double len = (x * x) + (y * y);
		return Math.sqrt(len);
	}

	/**
	 * Calculates a vector's cross product.
	 *
	 * @param vector2D
	 *            The second vector.
	 */
	public double cross(Vector2D vector2D) {
		return x * vector2D.y - y * vector2D.x;
	}

	/**
	 * Return this vector object as a string.
	 */
	public String toString() {
		return "Vector2D(" + x + ", " + y + ")";
	}

	// -------------------------------------------------------------------------
	// Getters and setters.
	// -------------------------------------------------------------------------

	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}
}